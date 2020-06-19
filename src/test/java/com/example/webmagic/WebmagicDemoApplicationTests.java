package com.example.webmagic;

import com.example.webmagic.dao.ProxyIpRepository;
import com.example.webmagic.dao.ZhihuHotRepository;
import com.example.webmagic.entry.SpiderBootstrapper;
import com.example.webmagic.pageprocessor.ZhihuHotPageProcessor;
import com.example.webmagic.pipeline.ZhihuHotPipeline;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.webmagic.constant.UrlConstant.BASE_URL_ZHIHU_HOT;

@ActiveProfiles("dev")
@Slf4j
@SpringBootTest
class WebmagicDemoApplicationTests {
    @Autowired
    private SpiderBootstrapper bootstrapper;
    @Autowired
    private ZhihuHotRepository zhihuHotRepository;

    @Test
    public void initSpider() {
//        bootstrapper.run(BASE_URL_DOULIST_PRIMARY, new DoubanDoulistPageProcessor(), new DoubanDoulistPipeline(), "data");
        bootstrapper.run(BASE_URL_ZHIHU_HOT, new ZhihuHotPageProcessor(), new ZhihuHotPipeline(zhihuHotRepository), "data");
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
