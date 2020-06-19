package com.example.webmagic.pageprocessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
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
}
