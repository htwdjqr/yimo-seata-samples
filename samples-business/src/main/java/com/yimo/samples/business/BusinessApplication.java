package com.yimo.samples.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 这是描述
 *
 * @author 会跳舞的机器人
 * @date 2024/1/12
 */
@SpringBootApplication(scanBasePackages = "com.yimo.samples")
public class BusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessApplication.class, args);
    }

}
