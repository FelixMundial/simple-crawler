package com.example.webmagic;

import com.example.webmagic.dao.ProxyIpRepository;
import com.example.webmagic.dao.ZhihuHotRepository;
import com.example.webmagic.entry.SpiderBootstrapper;
import com.example.webmagic.pageprocessor.BaiduTopPageProcessor;
import com.example.webmagic.pageprocessor.DoubanDoulistPageProcessor;
import com.example.webmagic.pageprocessor.ZhihuHotPageProcessor;
import com.example.webmagic.pipeline.BaiduTopPipeline;
import com.example.webmagic.pipeline.DoubanDoulistPipeline;
import com.example.webmagic.pipeline.ZhihuHotPipeline;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.webmagic.constant.UrlConstant.BASE_URL_ZHIHU_HOT;

@ActiveProfiles("dev")
@Slf4j
@SpringBootTest
class WebmagicDemoApplicationTests {
    @Value("${output.file-path-prefix.bilibili}")
    private String dataOutputPathPrefix;

    @Autowired
    private ZhihuHotRepository zhihuHotRepository;

    @Autowired
    private SpiderBootstrapper bootstrapper;

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

    @Test
    public void initSpider() {
//        bootstrapper.run(BASE_URL_DOULIST_TEST2, doubanDoulistPageProcessor, doubanDoulistPipeline, dataOutputPathPrefix);
//        bootstrapper.run(BASE_URL_BAIDU_HOT, baiduTopPageProcessor, baiduTopPipeline, dataOutputPathPrefix);
        bootstrapper.run(BASE_URL_ZHIHU_HOT, zhihuHotPageProcessor, zhihuHotPipeline, dataOutputPathPrefix);
    }

    //    @Autowired
    private ProxyIpRepository proxyIpRepository;

    @Test
    public void initProxyIpSpider() {
        /*Spider.create(new ProxyIpPageProcessor())
                .addUrl(BASE_URL_XICIDAILI + 1)
                .thread(THREAD_NUM).setExitWhenComplete(true)
                .addPipeline(new ProxyIpPipeline(proxyIpRepository))
                .run();*/
    }

}
