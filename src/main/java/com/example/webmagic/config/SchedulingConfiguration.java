package com.example.webmagic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yinfelix
 * @date 2020/6/21
 */
@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Bean("taskExecutor")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("Task-");
        scheduler.setAwaitTerminationSeconds(30 * 60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }

    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        TaskScheduler taskScheduler = taskScheduler();
//        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
