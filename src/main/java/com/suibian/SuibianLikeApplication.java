package com.suibian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.suibian.mapper")
public class SuibianLikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuibianLikeApplication.class, args);
    }
}
