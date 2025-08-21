package com.mshando.biddingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Bidding Service.
 * 
 * This service manages the bidding functionality for the Mshando platform,
 * including bid creation, acceptance, rejection, and lifecycle management.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BiddingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiddingServiceApplication.class, args);
    }
}
