package com.example.controller;

import com.example.kafka.Event;
import com.example.kafka.KafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {
    private final KafkaProducer kafkaProducer;

    KafkaController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody Event message){
        kafkaProducer.sendMessage("topic", message);
        return "Message sent: " + message;
    }

    @PostMapping("/send/topic/{topic}/key/{key}")
    public String sendKeyMessage(@PathVariable String topic, @PathVariable String key, @RequestBody Event message){
        kafkaProducer.sendKeyMessage(topic, key, message);
        return "Key = " + key + " Message sent: " + message;
    }

    @PostMapping("/send/many/{topic}")
    public String sendManyMessage(@PathVariable String topic, @RequestBody Event message){
        for(int i = 0; i < 10000; i++){
            message.setId(i);
            kafkaProducer.sendMessage(topic, message);
        }
        return "Message sent: " + message;
    }

    @PostMapping("/send/many/{topic}/key/{key}")
    public String sendKeyWithMessage(@PathVariable String topic, @PathVariable String key, @RequestBody Event message){
        for(int i = 0; i < 10000; i++){
            message.setId(i);
            kafkaProducer.sendKeyMessage(topic, key, message);
        }
        return "Key = " + key + " Message sent: " + message;
    }
}
