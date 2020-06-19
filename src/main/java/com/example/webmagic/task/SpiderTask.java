package com.example.webmagic.task;

import com.example.webmagic.dao.BilibiliRankingRepository;
import com.example.webmagic.dao.ZhihuHotRepository;
import com.example.webmagic.entry.SpiderBootstrapper;
import com.example.webmagic.pageprocessor.BilibiliRankingPageProcessor;
import com.example.webmagic.pageprocessor.ZhihuHotPageProcessor;
import com.example.webmagic.pipeline.BilibiliRankingPipeline;
import com.example.webmagic.pipeline.ZhihuHotPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.example.webmagic.constant.UrlConstant.BASE_URL_BILIBILI_HOT;
import static com.example.webmagic.constant.UrlConstant.BASE_URL_ZHIHU_HOT;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@Component
@Slf4j
@EnableScheduling
public class SpiderTask {
    @Value("${spring.task.scheduling.delay}")
    public int taskDelayRange;
    @Value("${output.file-path-prefix.bilibili}")
    private String bilibiliDataOutputPathPrefix;
    @Value("${output.file-path-prefix.zhihu}")
    private String zhihuDataOutputPathPrefix;

    @Autowired
    private SpiderBootstrapper bootstrapper;
    @Autowired
    private BilibiliRankingRepository bilibiliRankingRepository;
    @Autowired
    private ZhihuHotRepository zhihuHotRepository;

    @Scheduled(cron = "${spring.task.scheduling.rule.bilibili}")
    public void initBilibiliSpiderTask() throws InterruptedException {
        log.info("initBilibiliSpiderTask started");
        if (taskDelayRange > 0) {
            Thread.sleep(new Random().nextInt(taskDelayRange) * 60 * 1000);
        }
        bootstrapper.run(BASE_URL_BILIBILI_HOT, new BilibiliRankingPageProcessor(), new BilibiliRankingPipeline(bilibiliRankingRepository), bilibiliDataOutputPathPrefix);
        log.info("initBilibiliSpiderTask ended");
    }

    @Scheduled(cron = "${spring.task.scheduling.rule.zhihu}")
    public void initZhihuSpiderTask() throws InterruptedException {
        log.info("initZhihuSpiderTask started");
        if (taskDelayRange > 0) {
            Thread.sleep(new Random().nextInt(taskDelayRange) * 60 * 1000);
        }
        bootstrapper.run(BASE_URL_ZHIHU_HOT, new ZhihuHotPageProcessor(), new ZhihuHotPipeline(zhihuHotRepository), zhihuDataOutputPathPrefix);
        log.info("initZhihuSpiderTask ended");
    }
}
