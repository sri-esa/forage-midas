package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    
    private final RestTemplate restTemplate;
    private final String incentiveApiUrl;

    public IncentiveService(
            RestTemplate restTemplate,
            @Value("${incentive.api.url}") String incentiveApiUrl) {
        this.restTemplate = restTemplate;
        this.incentiveApiUrl = incentiveApiUrl;
    }

    public Incentive getIncentive(Transaction transaction) {
        try {
            logger.info("Fetching incentive for transaction: {}", transaction);
            Incentive incentive = restTemplate.postForObject(
                incentiveApiUrl,
                transaction,
                Incentive.class
            );
            logger.info("Received incentive: {}", incentive);
            return incentive;
        } catch (Exception e) {
            logger.error("Failed to fetch incentive for transaction: {}", transaction, e);
            return new Incentive(0); // Default to no incentive on error
        }
    }
} 