package com.borovkov.srv.service;

import com.borovkov.srv.dto.notifications.NotificationDTO;
import com.borovkov.srv.models.NotificationType;
import com.borovkov.srv.models.Task;
import com.borovkov.srv.producer.RabbitMQStatusProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${message-processor.url}")
    private String messageProcessorUrl;

    public String generationNotificationMessage(Task task, String message, NotificationType type) {

        NotificationDTO dto = NotificationDTO.builder()
                .userId(task.getAssignedTo())
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .build();

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Kafka notification message", e);
        }
    }

    public List<NotificationDTO> getNotification(Long userId) {
        String url = messageProcessorUrl + "/api/notifications/" + userId;
        ResponseEntity<NotificationDTO[]> response = restTemplate.getForEntity(url, NotificationDTO[].class);
        return Arrays.asList(response.getBody());
    }

    public void markNotificationAsRead(Long notificationId, Long userId) {
        String url = messageProcessorUrl + "/api/notifications/" + notificationId + "/read?userId=" + userId;
        restTemplate.put(url, HttpEntity.EMPTY);
    }

    public void registerRabbitMQNotification(RabbitMQStatusProducer producer, String message) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                producer.sendNotification(message);
            }
        });
    }

}
