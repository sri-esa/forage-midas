package com.jpmc.midascore.controller;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    private final DatabaseConduit databaseConduit;

    public BalanceController(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") long userId) {
        logger.info("Received balance request for user: {}", userId);
        UserRecord user = databaseConduit.findById(userId);
        if (user == null) {
            logger.info("User {} not found, returning zero balance", userId);
            return new Balance(0.0f);
        }
        float balance = user.getBalance();
        logger.info("Returning balance for user {}: {}", userId, balance);
        return new Balance(balance);
    }
} 