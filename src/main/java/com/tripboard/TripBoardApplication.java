package com.tripboard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.tripboard.mapper")
@EnableAsync
public class TripBoardApplication {
    public static void main(String[] args) {
        System.out.println("\n\n=======================================================");
        System.out.println(">>> 🔥🔥🔥 NEW CODE IS RUNNING !!! (VER: MAP FIX) 🔥🔥🔥");
        System.out.println("=======================================================\n\n");
        SpringApplication.run(TripBoardApplication.class, args);
    }
}
