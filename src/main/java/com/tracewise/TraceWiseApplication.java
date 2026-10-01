package com.tracewise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TraceWiseApplication {

    public static void main(String[] args) {
        // AppConfig.java ab automatically topology handle karega
        SpringApplication.run(TraceWiseApplication.class, args);
    }
    
}