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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Random;

import static com.example.webmagic.constant.UrlConstant.*;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@Component
@Slf4j
@EnableAsync
@EnableScheduling
public class SpiderTask {
    @Value("${spring.task.scheduling.delay}")
    public int taskDelayRange;
    @Value("${output.file-path-prefix.bilibili}")
    private String bilibiliDataOutputPathPrefix;
    @Value("${output.file-path-prefix.zhihu}")
    private String zhihuDataOutputPathPrefix;
    @Value("${output.file-path-prefix.baidu}")
    private String baiduDataOutputPathPrefix;

    @Autowired
    private SpiderBootstrapper bootstrapper;

    @Async("taskExecutor")
    @Scheduled(cron = "${spring.task.scheduling.rule.bilibili}")
    public void initBilibiliSpiderTask() throws InterruptedException {
        initTask(BASE_URL_BILIBILI_HOT, bilibiliRankingPageProcessor, bilibiliRankingPipeline, bilibiliDataOutputPathPrefix);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "${spring.task.scheduling.rule.zhihu}")
    public void initZhihuSpiderTask() throws InterruptedException {
        initTask(BASE_URL_ZHIHU_HOT, zhihuHotPageProcessor, zhihuHotPipeline, zhihuDataOutputPathPrefix);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "${spring.task.scheduling.rule.baidu}")
    public void initBaiduBaiduTask() throws InterruptedException {
        initTask(BASE_URL_BAIDU_HOT, baiduTopPageProcessor, baiduTopPipeline, baiduDataOutputPathPrefix);
    }

    private void initTask(String targetUrl, PageProcessor pageProcessor, Pipeline pipeline, String dataOutputPathPrefix) throws InterruptedException {
        String taskEntityName = pageProcessor.getClass().getSimpleName();
        String taskName = taskEntityName.substring(0, taskEntityName.indexOf("PageProcessor"));
        log.info("{}Task started", taskName);
        if (taskDelayRange > 0) {
            Thread.sleep(new Random().nextInt(taskDelayRange) * 60 * 1000);
        }
        bootstrapper.run(targetUrl, pageProcessor, pipeline, dataOutputPathPrefix);
        log.info("{}Task ended", taskName);
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
}
