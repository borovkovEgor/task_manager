package com.borovkov.srv.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class Comment {
    private String id;
    private Long taskId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isEdited;
    private LocalDateTime modifiedAt;
    private Long groupId;
}
