package com.example.spring.consumer.service;

import com.example.spring.consumer.dto.Message;

public interface ConsumerService {

    void action(Message message) throws Exception;

}
