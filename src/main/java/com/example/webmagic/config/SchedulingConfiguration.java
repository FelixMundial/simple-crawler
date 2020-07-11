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

    @Bean("taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("Task-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }

    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        /*
        若不在Task方法上显式标注@Async()注解并指定ThreadPoolTaskScheduler，则只能执行阻塞式任务
        （直接在ScheduledTaskRegistrar中配置TaskScheduler不起作用）
         */
//        taskRegistrar.setTaskScheduler(taskScheduler());
    }
}
