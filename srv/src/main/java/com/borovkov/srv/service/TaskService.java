package com.borovkov.srv.service;

import com.borovkov.srv.dto.response.TaskResponseDto;
import com.borovkov.srv.mapper.CommonMapper;
import com.borovkov.srv.models.NotificationType;
import com.borovkov.srv.models.Task;
import com.borovkov.srv.models.User;
import com.borovkov.srv.producer.RabbitMQStatusProducer;
import com.borovkov.srv.repositories.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final UserService userService;
    private final CommonMapper commonMapper;
    private final RabbitMQStatusProducer rabbitMQStatusProducer;
    private final NotificationService notificationService;

    public Task getTaskById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task with id={ %d } not found".formatted(id)));
    }

    public Page<Task> getAllTasks(Specification<Task> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    public TaskResponseDto getByIdForController(Long id, Principal principal) {
        Task task = getTaskById(id);
        User user = userService.getUserByUsername(principal.getName());

        if (!task.getGroupId().equals(user.getId()) && !task.getGroupId().equals(user.getCreatedBy())) {
            throw new AccessDeniedException("You are not allowed to view this task as it does not belong to your group and you are not the creator");
        }

        return initTaskResponseDto(task);
    }

    @Transactional
    public TaskResponseDto createForController(Task task, Principal principal) {
        Task savedTask = create(task, principal);
        return initTaskResponseDto(savedTask);
    }

    @Transactional
    public Task create(Task task, Principal principal) {

        if (task.getAssignedTo() == null || task.getTitle() ==  null) {
            throw new IllegalArgumentException("Fields \"assignedTo\" and \"title\" must not be null");
        }

        userService.checkExistsUserById(task.getAssignedTo());
        User user = userService.getUserByUsername(principal.getName());

        task.setId(null);
        task.setCreatedBy(user.getId());
        task.setStatus(Task.STATUS.NEW);
        task.setGroupId(userService.getGroupId(user));

        Task savedTask = repository.save(task);

        String message = String.format("Создана новая задача: %s", savedTask.getTitle());
        String notificationMessage = notificationService.generationNotificationMessage(savedTask, message, NotificationType.TASK_CREATED);

        notificationService.registerRabbitMQNotification(rabbitMQStatusProducer, notificationMessage);

        return savedTask;
    }

    @Transactional
    public TaskResponseDto updateForController(Task task, Principal principal) {
        Task updatedTask = update(task, principal);
        return initTaskResponseDto(updatedTask);
    }

    @Transactional
    public Task update(Task updatedTask, Principal principal) {
        if (updatedTask.getAssignedTo() == null || updatedTask.getTitle() ==  null) {
            throw new IllegalArgumentException("AssignedTo and Title must not be null");
        }

        checkUpdateTask(updatedTask, principal);

        Task oldTask = getTaskById(updatedTask.getId());

        oldTask.setTitle(updatedTask.getTitle());
        oldTask.setDescription(updatedTask.getDescription());
        oldTask.setAssignedTo(updatedTask.getAssignedTo());
        oldTask.setDeadline(updatedTask.getDeadline());

        if (!oldTask.getStatus().equals(updatedTask.getStatus())) {
            oldTask.setStatus(updatedTask.getStatus());

            Task savedTask = repository.save(oldTask);

            String message = String.format("Статус задачи '%s' обновлен на - '%s'", savedTask.getTitle(), savedTask.getStatus());
            String notificationMessage = notificationService.generationNotificationMessage(savedTask, message, NotificationType.TASK_STATUS_UPDATED);

            notificationService.registerRabbitMQNotification(rabbitMQStatusProducer, notificationMessage);

            return savedTask;
        }

        return repository.save(oldTask);
    }

    @Transactional
    public List<Long> deleteTasks(List<Long> ids, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        if (!User.Role.ROLE_ADMIN.equals(admin.getRole())) {
            throw new AccessDeniedException("Only an administrator can delete tasks");
        }
        checkAdminGroupId(admin.getId(), ids);

        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                deletedIds.add(id);
            } else {
                throw new EntityNotFoundException(String.format("Task with id={ %d } not found", id));
            }
        }

        return deletedIds;
    }

    public void checkExistsTaskById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(String.format("Task with id={ %d } not found", id));
        }
    }

    private void checkUpdateTask(Task task, Principal principal) {
        Task oldTask = getTaskById(task.getId());
        User user = userService.getUserByUsername(principal.getName());

        // Проверяем права, таску могут менять только ее создатель и админ группы
        if (User.Role.ROLE_ADMIN.equals(user.getRole())) {
            if (!oldTask.getCreatedBy().equals(user.getId()) && !oldTask.getGroupId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to modify this task. Only the task creator or group admin can perform this action");
            }
        } else {
            if (!oldTask.getCreatedBy().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to modify this task. Only the task creator or group admin can perform this action");
            }
        }
    }

    private TaskResponseDto initTaskResponseDto(Task task) {

        User assignedToUser = userService.getUserById(task.getAssignedTo());
        User createdByUser = userService.getUserById(task.getCreatedBy());

        TaskResponseDto response = commonMapper.toTaskResponseDto(task);
        response.setUsernameAssignedTo(assignedToUser.getUsername());
        response.setUsernameCreatedBy(createdByUser.getUsername());
        return response;
    }

    private void checkAdminGroupId(Long adminId, List<Long> taskIds) {
        taskIds.forEach(taskId -> {
            Task task = repository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException(String.format("Task with id={ %d } not found", taskId)));
            if (!adminId.equals(task.getGroupId())) {
                throw new AccessDeniedException("You are not allowed to delete tasks from another group");
            }
        });
    }
}
