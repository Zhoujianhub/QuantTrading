package com.quant.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quant.account.repository")
public class QuantAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantAccountApplication.class, args);
    }
}
