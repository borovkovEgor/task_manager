package com.borovkov.srv.mapper;

import com.borovkov.srv.dto.request.CommentRequestDto;
import com.borovkov.srv.dto.request.TaskRequestDto;
import com.borovkov.srv.dto.request.UserRequestDto;
import com.borovkov.srv.dto.response.CommentResponseDto;
import com.borovkov.srv.dto.response.TaskResponseDto;
import com.borovkov.srv.dto.response.UserResponseDto;
import com.borovkov.srv.models.Comment;
import com.borovkov.srv.models.Task;
import com.borovkov.srv.models.User;
import org.springframework.stereotype.Component;


@Component
public class CommonMapper {

    // ------------------- User -------------------

    public User toUserJpa(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());

        return user;
    }

    public User toUserJpa(UserResponseDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setRole(dto.getRole());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());

        return user;
    }

    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    public UserRequestDto toUserRequestDto(User user) {
        if (user == null) {
            return null;
        }

        UserRequestDto dto = new UserRequestDto();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());

        return dto;
    }

    // ------------------- Task -------------------

    public Task toTaskJpa(TaskRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setAssignedTo(dto.getAssignedTo());
        task.setDeadline(dto.getDeadline());

        return task;
    }

    public Task toTaskJpa(TaskResponseDto dto) {
        if (dto == null) {
            return null;
        }

        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());

        return task;
    }

    public TaskResponseDto toTaskResponseDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setDeadline(task.getDeadline());

        return dto;
    }

    public TaskRequestDto toTaskRequestDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskRequestDto dto = new TaskRequestDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setDeadline(task.getDeadline());

        return dto;
    }

    // ------------------- Comment -------------------

    public Comment toCommentJpa(CommentRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setContent(dto.getContent());

        return comment;
    }

    public Comment toCommentJpa(CommentResponseDto dto) {
        if (dto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setTaskId(dto.getTaskId());
        comment.setUserId(dto.getUserId());
        comment.setContent(dto.getContent());
        comment.setCreatedAt(dto.getCreatedAt());
        comment.setIsEdited(dto.getIsEdited());
        comment.setModifiedAt(dto.getModifiedAt());
        comment.setGroupId(dto.getGroupId());

        return comment;
    }

    public CommentResponseDto toCommentResponseDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTaskId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setIsEdited(comment.getIsEdited());
        dto.setModifiedAt(comment.getModifiedAt());
        dto.setGroupId(comment.getGroupId());

        return dto;
    }

    public CommentRequestDto toCommentRequestDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentRequestDto dto = new CommentRequestDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());

        return dto;
    }
}