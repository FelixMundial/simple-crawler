package com.example.webmagic.entry;

import com.example.webmagic.custom.EnhancedHttpClientDownloader;
import com.example.webmagic.service.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.example.webmagic.constant.SpiderConstant.THREAD_NUM;
import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Component
@Slf4j
public class SpiderBootstrapper {
    @Autowired
    private EnhancedHttpClientDownloader downloader;
    @Autowired
    private ProxyService proxyService;

    public void run(String targetUrl, PageProcessor pageProcessor, Pipeline pipeline, String dataOutputPathPrefix) {
        Spider spider = Spider.create(pageProcessor)
                .addUrl(targetUrl)
                .thread(THREAD_NUM).setExitWhenComplete(true)
                .addPipeline(new JsonFilePipeline(dataOutputPathPrefix + "/" + LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI))))
                .addPipeline(pipeline);

        /*
        在第一次下载前强制刷新代理
         */
        proxyService.refreshDownloaderProxy(downloader);
        spider.setDownloader(downloader).run();
    }

    public void kill() {

    }
}
