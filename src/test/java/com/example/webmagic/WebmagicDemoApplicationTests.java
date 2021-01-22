package com.example.webmagic;

import com.example.webmagic.entry.SpiderBootstrapper;
import com.example.webmagic.pageprocessor.BaiduTopPageProcessor;
import com.example.webmagic.pageprocessor.BilibiliRankingPageProcessor;
import com.example.webmagic.pageprocessor.ZhihuHotPageProcessor;
import com.example.webmagic.pipeline.BaiduTopPipeline;
import com.example.webmagic.pipeline.BilibiliRankingPipeline;
import com.example.webmagic.pipeline.ZhihuHotPipeline;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.webmagic.constant.UrlConstant.BASE_URL_BILIBILI_HOT;

@ActiveProfiles("test")
@Slf4j
@SpringBootTest
class WebmagicDemoApplicationTests {
    @Value("${output.file-path-prefix.bilibili}")
    private String outputPathPrefix;

    @Autowired
    SpiderBootstrapper bootstrapper;
    @Autowired
    ZhihuHotPageProcessor zhihuHotPageProcessor;
    @Autowired
    BilibiliRankingPageProcessor bilibiliRankingPageProcessor;
    @Autowired
    BaiduTopPageProcessor baiduTopPageProcessor;
    @Autowired
    ZhihuHotPipeline zhihuHotPipeline;
    @Autowired
    BilibiliRankingPipeline bilibiliRankingPipeline;
    @Autowired
    BaiduTopPipeline baiduTopPipeline;

    @Test
    void initTask() {
//        bootstrapper.run(BASE_URL_ZHIHU_HOT, zhihuHotPageProcessor, zhihuHotPipeline, outputPathPrefix);
        bootstrapper.run(BASE_URL_BILIBILI_HOT, bilibiliRankingPageProcessor, bilibiliRankingPipeline, outputPathPrefix);
//        bootstrapper.run(BASE_URL_BAIDU_HOT, baiduTopPageProcessor, baiduTopPipeline, outputPathPrefix);
    }
}
