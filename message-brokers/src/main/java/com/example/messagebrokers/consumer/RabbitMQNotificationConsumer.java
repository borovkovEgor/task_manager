package com.example.messagebrokers.consumer;

import com.example.messagebrokers.models.Notification;
import com.example.messagebrokers.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQNotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${spring.rabbitmq.template.default-receive-queue}")
    public void listen(String message) {
        System.out.println("Received status update from RabbitMQ: " + message);

        try {
            Notification notification = objectMapper.readValue(message, Notification.class);
            notificationRepository.save(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}
