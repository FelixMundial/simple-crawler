package com.example.webmagic.task;

import com.example.webmagic.entry.SpiderBootstrapper;
import com.example.webmagic.pageprocessor.BaiduTopPageProcessor;
import com.example.webmagic.pageprocessor.BilibiliRankingPageProcessor;
import com.example.webmagic.pageprocessor.DoubanDoulistPageProcessor;
import com.example.webmagic.pageprocessor.ZhihuHotPageProcessor;
import com.example.webmagic.pipeline.BaiduTopPipeline;
import com.example.webmagic.pipeline.BilibiliRankingPipeline;
import com.example.webmagic.pipeline.DoubanDoulistPipeline;
import com.example.webmagic.pipeline.ZhihuHotPipeline;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Random;

import static com.example.webmagic.constant.UrlConstant.*;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@ConfigurationProperties(prefix = "spring.task.spider")
@Component
@Slf4j
@EnableAsync
@EnableScheduling
public class SpiderTask {
    @Getter
    @Setter
    private String delay;

    @Autowired
    private SpiderBootstrapper bootstrapper;

    @Async
    @Scheduled(cron = "${spring.task.scheduling.rule.bilibili}")
    public void initBilibiliSpiderTask() throws InterruptedException {
        initTask(BASE_URL_BILIBILI_HOT, bilibiliRankingPageProcessor, bilibiliRankingPipeline, bilibiliDataOutputPathPrefix);
    }

    @Async
    @Scheduled(cron = "${spring.task.scheduling.rule.zhihu}")
    public void initZhihuSpiderTask() throws InterruptedException {
        initTask(BASE_URL_ZHIHU_HOT, zhihuHotPageProcessor, zhihuHotPipeline, zhihuDataOutputPathPrefix);
    }

    @Async
    @Scheduled(cron = "${spring.task.scheduling.rule.baidu}")
    public void initBaiduBaiduTask() throws InterruptedException {
        initTask(BASE_URL_BAIDU_HOT, baiduTopPageProcessor, baiduTopPipeline, baiduDataOutputPathPrefix);
    }

    public void initTask(String targetUrl, PageProcessor pageProcessor, Pipeline pipeline, String dataOutputPathPrefix) throws InterruptedException {
        String taskEntityName = pageProcessor.getClass().getSimpleName();
        String taskName = taskEntityName.substring(0, taskEntityName.indexOf("PageProcessor"));
        int intDelay = Integer.parseInt(delay);
        log.info("{}Task started", taskName);
        if (intDelay > 0) {
            Thread.sleep(new Random().nextInt(intDelay) * 60 * 1000);
        }
        bootstrapper.run(targetUrl, pageProcessor, pipeline, dataOutputPathPrefix);
        log.info("{}Task ended", taskName);
        doTaskCleaning();
    }

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public void doTaskCleaning() {
        int activeThreadCount = taskExecutor.getActiveCount();
        if (activeThreadCount >= 1) {
            // todo: 邮件预警
            log.warn("当前活跃线程数：{}", activeThreadCount);
        } else {
            log.info("当前活跃线程数：{}", activeThreadCount);
        }
    }

    @Autowired
    private BilibiliRankingPageProcessor bilibiliRankingPageProcessor;
    @Autowired
    private BilibiliRankingPipeline bilibiliRankingPipeline;
    @Autowired
    private ZhihuHotPageProcessor zhihuHotPageProcessor;
    @Autowired
    private ZhihuHotPipeline zhihuHotPipeline;
    @Autowired
    private DoubanDoulistPageProcessor doubanDoulistPageProcessor;
    @Autowired
    private DoubanDoulistPipeline doubanDoulistPipeline;
    @Autowired
    private BaiduTopPageProcessor baiduTopPageProcessor;
    @Autowired
    private BaiduTopPipeline baiduTopPipeline;

    @Value("${output.file-path-prefix.bilibili}")
    private String bilibiliDataOutputPathPrefix;
    @Value("${output.file-path-prefix.zhihu}")
    private String zhihuDataOutputPathPrefix;
    @Value("${output.file-path-prefix.baidu}")
    private String baiduDataOutputPathPrefix;
}
