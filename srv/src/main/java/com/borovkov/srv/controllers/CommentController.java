package com.borovkov.srv.controllers;

import com.borovkov.srv.dto.request.CommentRequestDto;
import com.borovkov.srv.dto.response.CommentResponseDto;
import com.borovkov.srv.mapper.CommonMapper;
import com.borovkov.srv.models.Comment;
import com.borovkov.srv.models.User;
import com.borovkov.srv.service.CommentsService;
import com.borovkov.srv.service.UserService;
import com.borovkov.srv.utils.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentsService commentsService;
    private final UserService userService;
    private final CommonMapper commonMapper;

    @Operation(summary = "Получение списка комментариев к задаче")
    @GetMapping("/{taskId:\\d+}/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByTask(
            @PathVariable
            Long taskId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(description = "Параметры сортировки, например: 'createdAt,desk'")
            @RequestParam(required = false)
            String sort,

            Principal principal
    ) {
        Sort sortOrder = SortUtils.buildSort(sort, Comment.class);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        User user = userService.getUserByUsername(principal.getName());

        Page<Comment> commentPage = commentsService.getCommentsByTaskIdAndGroupId(taskId, userService.getGroupId(user), pageable);
        Page<CommentResponseDto> response = commentPage.map(commonMapper::toCommentResponseDto);
        commentsService.initCommentResponseDto(response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Создание комментария")
    @PostMapping("/{taskId:\\d+}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable
            Long taskId,

            @RequestBody
            CommentRequestDto request,

            Principal principal
    ) {
        Comment comment = commonMapper.toCommentJpa(request);
        CommentResponseDto response = commentsService.createForController(taskId, comment, principal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Редактирование комментария")
    @PutMapping("/{taskId:\\d+}/edit")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CommentResponseDto> editComment(
            @PathVariable
            Long taskId,

            @RequestBody
            CommentRequestDto request,

            Principal principal
    ) {
        Comment comment = commonMapper.toCommentJpa(request);
        CommentResponseDto response = commentsService.editCommentForController(taskId, comment, principal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удаление комментария. Доступно для пользователей с ролью - ADMIN")
    @DeleteMapping("/{ids}/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> deleteComment(@PathVariable List<String> ids, Principal principal) {
        List<String> deletedIds = commentsService.deleteComments(ids, principal);
        return new ResponseEntity<>(deletedIds, HttpStatus.OK);
    }
}
