package com.borovkov.srv.controllers;

import com.borovkov.srv.dto.request.TaskRequestDto;
import com.borovkov.srv.dto.response.TaskResponseDto;
import com.borovkov.srv.mapper.CommonMapper;
import com.borovkov.srv.models.Task;
import com.borovkov.srv.models.User;
import com.borovkov.srv.service.TaskService;
import com.borovkov.srv.service.UserService;
import com.borovkov.srv.utils.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.borovkov.srv.utils.SpecificationsUtils.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final CommonMapper commonMapper;

    @Operation(summary = "Получение списка задач")
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(

            @Parameter(description = "Название задачи")
            @RequestParam(required = false)
            String title,

            @Parameter(description = "Описание задачи")
            @RequestParam(required = false)
            String description,

            @Parameter(description = "Статус задачи")
            @RequestParam(required = false)
            Task.STATUS status,

            @Parameter(description = "Имя пользователя, которому назначена задача")
            @RequestParam(required = false)
            String assignedTo,

            @Parameter(description = "Имя пользователя, назначившего задачу")
            @RequestParam(required = false)
            String createdBy,

            @Parameter(description = "Дата создания задачи. Фильтрация от переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdAtFrom,

            @Parameter(description = "Дата создания задачи. Фильтрация до переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdAtTo,

            @Parameter(description = "Дата обновления задачи. Фильтрация от переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime updatedAtFrom,

            @Parameter(description = "Дата обновления задачи. Фильтрация до переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime updatedAtTo,

            @Parameter(description = "Дедлайн задачи. Фильтрация от переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deadlineFrom,

            @Parameter(description = "Дедлайн задачи. Фильтрация до переданной даты")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deadlineTo,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(description = "Параметры сортировки, например: 'createdAt,desk'")
            @RequestParam(required = false)
            String sort,

            Principal principal
    ) {

        User user = userService.getUserByUsername(principal.getName());
        Long groupId = userService.getGroupId(user);

        Specification<Task> spec = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            equal(predicates, builder, root, "groupId", groupId);
            ilike(predicates, builder, root, "title", title, false);
            ilike(predicates, builder, root, "description", description, false);
            if (status != null)
                ilike(predicates, builder, root, "status", status.name(), true);
            dateFrom(predicates, builder, root, "createdAt", createdAtFrom);
            dateTo(predicates, builder, root, "createdAt", createdAtTo);
            dateFrom(predicates, builder, root, "updatedAt", updatedAtFrom);
            dateTo(predicates, builder, root, "updatedAt", updatedAtTo);
            dateFrom(predicates, builder, root, "deadline", deadlineFrom);
            dateTo(predicates, builder, root, "deadline", deadlineTo);

            List<Long> assignedToIds = (assignedTo != null) ? userService.getIdsByUsernameIgnoreCaseContaining(assignedTo) : null;
            List<Long> createdByIds = (createdBy != null) ? userService.getIdsByUsernameIgnoreCaseContaining(createdBy) : null;

            if (assignedToIds != null && createdByIds != null) {
                assignedToIds.addAll(createdByIds);
            } else if (assignedToIds == null) {
                assignedToIds = createdByIds;
            }

            if (assignedToIds != null && !assignedToIds.isEmpty()) {
                predicates.add(root.get("assignedTo").in(assignedToIds));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortOrder = SortUtils.buildSort(sort, Task.class);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<Task> taskPage = taskService.getAllTasks(spec, pageable);
        Page<TaskResponseDto> response = taskPage.map(commonMapper::toTaskResponseDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Получение задачи по id")
    @GetMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id, Principal principal) {
        TaskResponseDto response = taskService.getByIdForController(id, principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Создание задачи")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto request, Principal principal) {
        Task task = commonMapper.toTaskJpa(request);
        TaskResponseDto response = taskService.createForController(task, principal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновление задачи")
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TaskResponseDto> updateTaskForUser(@RequestBody TaskRequestDto request, Principal principal) {
        Task task = commonMapper.toTaskJpa(request);
        TaskResponseDto response = taskService.updateForController(task, principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Long>> deleteTasks(@PathVariable List<Long> ids, Principal principal) {
        List<Long> deletedIds = taskService.deleteTasks(ids, principal);
        return ResponseEntity.ok(deletedIds);
    }
}
