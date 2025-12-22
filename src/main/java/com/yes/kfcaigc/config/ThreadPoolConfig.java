package com.yes.kfcaigc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10, // 核心线程数
                20, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // 队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        
        return executor;
    }
}
