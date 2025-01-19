package com.borovkov.srv.service;

import com.borovkov.srv.dto.response.CommentResponseDto;
import com.borovkov.srv.mapper.CommonMapper;
import com.borovkov.srv.models.Comment;
import com.borovkov.srv.models.NotificationType;
import com.borovkov.srv.models.Task;
import com.borovkov.srv.models.User;
import com.borovkov.srv.producer.RabbitMQStatusProducer;
import com.borovkov.srv.repositories.CommentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository repository;
    private final UserService userService;
    private final TaskService taskService;
    private final CommonMapper commonMapper;
    private final RabbitMQStatusProducer rabbitMQStatusProducer;
    private final NotificationService notificationService;


    public Page<Comment> getCommentsByTaskIdAndGroupId(Long taskId, Long groupId, Pageable pageable) {
        return repository.findByTaskIdAndGroupId(taskId, groupId, pageable);
    }

    public Comment getCommentById(String commentId) {
        return repository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Comment with id={ %s } not found", commentId)));
    }

    @Transactional
    public CommentResponseDto createForController(Long taskId, Comment comment, Principal principal) {
        Comment savedComment = create(taskId, comment, principal);
        return initCommentResponseDto(savedComment);
    }

    @Transactional
    public Comment create(Long taskId, Comment comment, Principal principal) {
        checkUserAndTaskGroupMatch(taskId, principal);

        User user = userService.getUserByUsername(principal.getName());

        comment.setId(null);
        comment.setTaskId(taskId);
        comment.setUserId(user.getId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setIsEdited(false);
        comment.setModifiedAt(null);
        comment.setGroupId(userService.getGroupId(user));

        Comment savedComment = repository.save(comment);

        Task task = getTaskByComment(comment);
        String message = String.format("Задачу '%s' прокомментировал пользователь '%s'", task.getTitle(), user.getUsername());
        String notificationMessage = notificationService.generationNotificationMessage(task, message, NotificationType.TASK_COMMENTED);

        notificationService.registerRabbitMQNotification(rabbitMQStatusProducer, notificationMessage);

        return savedComment;
    }

    @Transactional
    public CommentResponseDto editCommentForController(Long taskId, Comment comment, Principal principal) {
        Comment editedComment = editComment(taskId, comment, principal);
        return initCommentResponseDto(editedComment);
    }

    @Transactional
    public Comment editComment(Long taskId, Comment comment, Principal principal) {
        checkUserAndTaskAndCommentGroupMatch(taskId, comment.getId(), principal);

        User user = userService.getUserByUsername(principal.getName());
        Comment oldComment = getCommentById(comment.getId());

        if (User.Role.ROLE_ADMIN.equals(user.getRole())) {
            if (!oldComment.getUserId().equals(user.getId()) && !oldComment.getGroupId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to modify this comment. Only the comment creator or group admin can perform this action");
            }
        } else {
            if (!oldComment.getUserId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to modify this comment. Only the comment creator or group admin can perform this action");
            }
        }

        oldComment.setContent(comment.getContent());
        oldComment.setIsEdited(true);
        oldComment.setModifiedAt(LocalDateTime.now());

        return repository.save(oldComment);
    }

    @Transactional
    public List<String> deleteComments(List<String> ids, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        if (!admin.getRole().equals(User.Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("User does not have admin rights to delete comments");
        }

        checkAdminGroupId(admin.getId(), ids);

        List<String> deletedIds = new ArrayList<>();

        for (String id : ids) {
            if (repository.findById(id).isPresent()) {
                repository.deleteById(id);
                deletedIds.add(id);
            } else {
                throw new EntityNotFoundException(String.format("Comment with id={ %s } not found", id));
            }
        }

        return deletedIds;
    }

    private Long checkUserAndTaskGroupMatch(Long taskId, Principal principal) {
        Task task = taskService.getTaskById(taskId);
        User user = userService.getUserByUsername(principal.getName());

        Long groupId = user.getCreatedBy() != null ? user.getCreatedBy() : user.getId();

        if (!task.getGroupId().equals(groupId)) {
            throw new AccessDeniedException("You are not allowed to access this task as it does not belong to your group");
        }

        return groupId;
    }

    private void checkUserAndTaskAndCommentGroupMatch(Long taskId, String commentId, Principal principal) {
        Long groupId = checkUserAndTaskGroupMatch(taskId, principal);
        Comment comment = getCommentById(commentId);

        if (!comment.getGroupId().equals(groupId)) {
            throw new AccessDeniedException("You are not allowed to access this task as it does not belong to your group");
        }

    }

    private CommentResponseDto initCommentResponseDto(Comment comment) {
        User user = userService.getUserById(comment.getUserId());

        CommentResponseDto dto = commonMapper.toCommentResponseDto(comment);
        dto.setUsername(user.getUsername());

        return dto;
    }

    private void checkAdminGroupId(Long adminId, List<String> commentIds) {
        commentIds.forEach(commentId -> {
            Comment comment = repository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException(String.format("Comment with id={ %s } not found", commentId)));
            if (!adminId.equals(comment.getGroupId())) {
                throw new AccessDeniedException("You are not allowed to delete comment from another group");
            }
        });
    }

    public void initCommentResponseDto(Page<CommentResponseDto> dtoPage) {
        dtoPage.forEach(dto -> {
            User user = userService.getUserById(dto.getUserId());
            dto.setUsername(user.getUsername());
        });
    }

    public Task getTaskByComment(Comment comment) {
        return taskService.getTaskById(comment.getTaskId());
    }
}
