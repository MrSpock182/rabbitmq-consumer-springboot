package com.example.spring.consumer.service.implementation;

import com.example.spring.consumer.dto.MessageQueue;
import com.example.spring.consumer.service.ConsumerService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final Logger log = Logger.getLogger(ConsumerServiceImpl.class.getName());

    @Override
    public void action(MessageQueue message) {
        if ("teste".equalsIgnoreCase(message.getText())) {
            throw new AmqpRejectAndDontRequeueException("Erro");
        }

        log.info(message.getText());
    }
}
