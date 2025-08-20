package com.mshando.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * User Service Application for user management and authentication
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    /**
     * Main method to start the User Service
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
