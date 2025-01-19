package com.borovkov.srv.dto.response;

import com.borovkov.srv.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}