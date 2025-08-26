package com.mshando.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Task Service Application for task management and categories
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TaskServiceApplication {

    /**
     * Main method to start the Task Service
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
