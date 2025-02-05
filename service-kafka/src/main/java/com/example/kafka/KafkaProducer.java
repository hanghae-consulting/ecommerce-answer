package com.example.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private final KafkaTemplate<String, Event> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Event> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Event message){
        kafkaTemplate.send(topic, message);
    }

    public void sendKeyMessage(String topic, String key, Event message){
        kafkaTemplate.send(topic, key, message);
    }
}
