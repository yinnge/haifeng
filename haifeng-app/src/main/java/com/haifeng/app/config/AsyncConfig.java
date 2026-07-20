package com.haifeng.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * haifeng-app 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * PDF Map 阶段并行执行线程池：
     * core=6, max=9, queue=20, CallerRunsPolicy（满载时由调用方线程执行，避免任务丢失）。
     * 共享单例，避免每次请求新建线程池。
     */
    @Bean(name = "pdfMapExecutor", destroyMethod = "shutdown")
    public ExecutorService pdfMapExecutor() {
        return new ThreadPoolExecutor(
                6,
                9,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("pdf-map-" + t.getId());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
