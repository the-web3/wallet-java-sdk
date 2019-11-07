package com.gingernet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WalletJavaSdkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletJavaSdkApplication.class, args);
    }
}
