package com.example.webmagic.pageprocessor;

import com.example.webmagic.util.UserAgentFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Collection;
import java.util.Random;

import static com.example.webmagic.constant.SpiderConstant.*;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
public abstract class SimpleListPageProcessor<T> implements PageProcessor {
    private final Site site = Site.me().setCharset(CHARSET_UTF_8)
            .setTimeOut(TIME_OUT).setRetryTimes(RETRY_TIMES).setCycleRetryTimes(CYCLE_RETRY_TIMES)
            .setSleepTime(BASE_SLEEP_INTERVAL + new Random().nextInt(BASE_SLEEP_INTERVAL / 2))
            .setUserAgent(UserAgentFactory.getUserAgent());

    public void setSiteCharset(String charset) {
        this.site.setCharset(charset);
    }

    /**
     * 从{@link us.codecraft.webmagic.Page}中解析实体类集合对象返回
     *
     * @param page {@link us.codecraft.webmagic.Page}
     * @return 所爬取的实体类集合对象
     */
    public abstract Collection<T> fetchItems(Page page);

    @Override
    public void process(Page page) {
        page.putField("items", fetchItems(page));
    }

    @Override
    public Site getSite() {
        return site;
    }
}
