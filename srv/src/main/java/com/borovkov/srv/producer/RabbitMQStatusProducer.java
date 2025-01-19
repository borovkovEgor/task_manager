package com.borovkov.srv.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQStatusProducer {


    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    public void sendNotification(String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        System.out.println("Status update sent to RabbitMQ: " + message);
    }
}
