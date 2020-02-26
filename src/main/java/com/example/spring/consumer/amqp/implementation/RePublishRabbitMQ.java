package com.example.spring.consumer.amqp.implementation;

import com.example.spring.consumer.amqp.AmqpRePublish;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class RePublishRabbitMQ implements AmqpRePublish {

    private final Logger log = Logger.getLogger(RePublishRabbitMQ.class.getName());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.request.exchenge.producer}")
    private String exchange;

    @Value("${spring.rabbitmq.request.routing-key.producer}")
    private String queue;

    @Value("${spring.rabbitmq.request.dead-letter.producer}")
    private String deadLetter;

    @Value("${spring.rabbitmq.request.parking-lot.producer}")
    private String parkingLot;

    private static final String X_RETRIES_HEADER = "x-retries";

    @Override
    @Scheduled(cron = "${spring.rabbitmq.listener.time-retry}")
    public void rePublish() {
        List<Message> list = getQueueMessages();

        list.forEach(message -> {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            Integer retriesHeader = (Integer) headers.get(X_RETRIES_HEADER);

            if (retriesHeader == null) {
                retriesHeader = 0;
            }

            if (retriesHeader < 3) {
                headers.put(X_RETRIES_HEADER, retriesHeader + 1);
                this.rabbitTemplate.send(exchange, queue, message);

                log.info("Tentou:" + retriesHeader + " - Date: " + new Date());
            } else {
                this.rabbitTemplate.send(parkingLot, message);
                log.info("Enviou para parkinglot - Date: " + new Date());
            }
        });
    }

    private List<Message> getQueueMessages() {
        List<Message> messages = new ArrayList<>();
        Boolean isNull;
        Message message;

        do {
            message = rabbitTemplate.receive(deadLetter);
            isNull = message != null;
            if (Boolean.TRUE.equals(isNull)) {
                messages.add(message);
            }
        }
        while (Boolean.TRUE.equals(isNull));

        return messages;
    }

}
