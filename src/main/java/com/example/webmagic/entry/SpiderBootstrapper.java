package com.example.webmagic.entry;

import com.example.webmagic.custom.CustomSpider;
import com.example.webmagic.custom.EnhancedHttpClientDownloader;
import com.example.webmagic.service.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.example.webmagic.constant.SpiderConstant.MAX_THREAD_NUM;
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
//    @Autowired
//    private GlobalSpiderListener spiderListener;

    public void run(String targetUrl, PageProcessor pageProcessor, Pipeline pipeline, String dataOutputPathPrefix) {
        CustomSpider spider = CustomSpider.create(pageProcessor)
                .addUrl(targetUrl)
                .thread(MAX_THREAD_NUM).setExitWhenComplete(true)
//                .setSpiderListeners(Collections.singletonList(spiderListener))
                .addPipeline(new JsonFilePipeline(dataOutputPathPrefix + "/" + LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI))))
                .addPipeline(pipeline);
        spider.setDownloader(downloader).run();
    }

    public void kill() {

    }
}
