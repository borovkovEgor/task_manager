package com.borovkov.srv.dto.response;

import com.borovkov.srv.models.Task;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponseDto {

    private Long id;
    private String title;
    private String description;
    private Task.STATUS status;
    private String usernameAssignedTo;
    private String usernameCreatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deadline;
}