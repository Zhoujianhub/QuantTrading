package com.quant.position;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.quant.position", "com.quant.common", "com.quant.monitor", "com.quant.notification"})
@EnableScheduling
@MapperScan({"com.quant.position.repository", "com.quant.monitor.repository", "com.quant.notification.repository"})
public class QuantPositionApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantPositionApplication.class, args);
    }
}
