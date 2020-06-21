package com.example.webmagic.pageprocessor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.example.webmagic.constant.SpiderConstant.BASE_SLEEP_INTERVAL;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Component
@Slf4j
public class DoubanDoulistPageProcessor extends SimpleListPageProcessor<String> {
    @Override
    public Collection<String> fetchItems(Page page) {
        List<String> bookIds = new ArrayList<>();

        log.debug("开始处理{}页面", page.getUrl());

        if (page.getHtml() != null) {
            List<Selectable> selectables = page.getHtml().xpath("//ul[@class='doulist-items']/li").nodes();

            if (selectables != null && selectables.size() > 0) {
                bookIds = new ArrayList<>(selectables.size());

                for (Selectable selectable : selectables) {
                    String bookUrl = selectable.xpath("//a/@href").get();
                    if (!StringUtils.isEmpty(bookUrl)) {
                        bookIds.add(bookUrl.substring(bookUrl.lastIndexOf("/") + 1));
                    }
                }
            }
        }

        log.debug("页面{}处理结束", page.getUrl());
        return bookIds;
    }

    @SneakyThrows
    @Override
    public void process(Page page) {
        Thread.sleep(new Random().nextInt(BASE_SLEEP_INTERVAL));
        if (page.getHtml() != null) {
            String targetPageUrl = page.getHtml().xpath("//section[@class='pagination']/a[@class='button next']").links().get();
            /*
            todo: 获取全局信息决定是否中止爬取
             */
            if (!StringUtils.isEmpty(targetPageUrl)) {
                page.addTargetRequest(targetPageUrl);
            }
        }
        page.putField("items", fetchItems(page));
    }
}
