package com.borovkov.srv.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private String id;
    private Long taskId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isEdited;
    private LocalDateTime modifiedAt;
    private Long groupId;
}