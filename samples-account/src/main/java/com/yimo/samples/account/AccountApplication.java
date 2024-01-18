package com.yimo.samples.account;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 这是描述
 *
 * @author 会跳舞的机器人
 * @date 2024/1/12
 */
@SpringBootApplication(scanBasePackages = "com.yimo.samples")
@EnableTransactionManagement
@MapperScan({"com.yimo.samples.account.dao"})
@EnableDubbo(scanBasePackages = "com.yimo.samples.account.dubbo")
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

}
