package com.yimo.samples.stock;

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
@MapperScan({"com.yimo.samples.stock.dao"})
@EnableDubbo(scanBasePackages = "com.yimo.samples.stock.dubbo")
public class StockApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }

}
