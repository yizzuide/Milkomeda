package com.github.yizzuide.milkomeda.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MilkomedaDemoApplication
 *
 * @author yizzuide
 * Create at 2019/03/30 19:04
 */
@SpringBootApplication
@EnableTransactionManagement
public class MilkomedaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilkomedaDemoApplication.class, args);
    }
}
