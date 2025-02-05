package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics="topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, Event> record) {
        System.out.println("Consumed message: " + record.value());
    }
}
