package com.jpmc.midascore.service;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.TransactionStatus;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);
    private final DatabaseConduit databaseConduit;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;
    private int transactionCount = 0;

    public TransactionProcessor(
            DatabaseConduit databaseConduit,
            TransactionRepository transactionRepository,
            IncentiveService incentiveService) {
        this.databaseConduit = databaseConduit;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void processTransaction(Transaction transaction) {
        transactionCount++;
        logger.info("Transaction #{} received - Amount: {}", transactionCount, transaction.getAmount());
        
        // Get sender and recipient records
        UserRecord sender = databaseConduit.findById(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findById(transaction.getRecipientId());
        
        // Validate transaction
        if (!isValidTransaction(transaction, sender, recipient)) {
            logger.error("Invalid transaction discarded: {}", transaction);
            return;
        }

        // Get incentive information
        Incentive incentive = incentiveService.getIncentive(transaction);
        float incentiveAmount = incentive != null ? incentive.getAmount() : 0f;
        
        // Create and save transaction record
        TransactionRecord record = new TransactionRecord(
            sender, 
            recipient, 
            transaction.getAmount(),
            incentiveAmount
        );
        
        // Update balances including incentive
        // Only deduct the transaction amount from sender
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        // Add both transaction amount and incentive to recipient
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // Save all changes in a single transaction
        databaseConduit.save(sender);
        databaseConduit.save(recipient);
        record.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(record);

        logger.info("Transaction processed successfully. Amount: {}, Incentive: {}", 
            transaction.getAmount(), incentiveAmount);
    }

    private boolean isValidTransaction(Transaction transaction, UserRecord sender, UserRecord recipient) {
        if (sender == null || recipient == null) {
            logger.error("Invalid transaction - user not found: {}", transaction);
            return false;
        }

        if (sender.getBalance() < transaction.getAmount()) {
            logger.error("Insufficient funds for transaction: {}", transaction);
            return false;
        }

        return true;
    }
} 