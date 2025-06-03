package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProducer.class);
    
    private final String topic;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public TransactionProducer(
            @Value("${general.kafka-topic}") String topic,
            KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(Transaction transaction) {
        logger.info("Sending transaction to Kafka: {}", transaction);
        kafkaTemplate.send(topic, transaction)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Transaction sent successfully: {}", transaction);
                } else {
                    logger.error("Failed to send transaction: {}", transaction, ex);
                }
            });
    }
} 