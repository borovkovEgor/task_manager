package com.borovkov.srv.dto.request;

import com.borovkov.srv.models.Task;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequestDto {

    private Long id;
    private String title;
    private String description;
    private Task.STATUS status;
    private Long assignedTo;
    private LocalDateTime deadline;
}