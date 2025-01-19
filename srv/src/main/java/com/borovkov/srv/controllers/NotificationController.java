package com.borovkov.srv.controllers;

import com.borovkov.srv.dto.notifications.NotificationDTO;
import com.borovkov.srv.models.User;
import com.borovkov.srv.service.NotificationService;
import com.borovkov.srv.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "Получение списка непрочитанных уведомлений по userId")
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<NotificationDTO>> getNotifications(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        List<NotificationDTO> notifications = notificationService.getNotification(user.getId());
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    @Operation(summary = "Обновление статуса уведомления")
    @PutMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        notificationService.markNotificationAsRead(notificationId, user.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
