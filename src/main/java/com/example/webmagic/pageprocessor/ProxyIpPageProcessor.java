package com.example.webmagic.pageprocessor;

import com.example.webmagic.entity.ProxyIp;
import com.example.webmagic.util.TimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.webmagic.constant.SpiderConstant.*;

/**
 * @author yinfelix
 * @date 2020/6/16
 * @deprecated 改用第三方方案，不再手动爬取代理站点
 */
@Slf4j
public class ProxyIpPageProcessor extends SimpleListPageProcessor<ProxyIp> {
    private static final int SLEEP_INTERVAL = BASE_SLEEP_INTERVAL / 2;

    private volatile AtomicInteger pageCounter = new AtomicInteger();

    @Override
    public Collection<ProxyIp> fetchItems(Page page) {
        List<ProxyIp> proxyIps = null;

        log.debug("开始处理页面：" + page.getUrl());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));

        if (page.getHtml() != null) {
            List<Selectable> selectables = page.getHtml().xpath("//table[@id='ip_list']/tbody/tr[@class='odd']").nodes();

            if (selectables != null && selectables.size() > 0) {
                proxyIps = new ArrayList<>(selectables.size());

                for (Selectable selectable : selectables) {
                    ProxyIp proxyIp = new ProxyIp();

                    float speed = Float.parseFloat(selectable.xpath("//td[7]/div/@title").get().replace("秒", ""));
                    float connTime = Float.parseFloat(selectable.xpath("//td[8]/div/@title").get().replace("秒", ""));
                    long survivingTimeInMinutes = TimeUtil.getTimeInMinutes(selectable.xpath("//td[9]/text()").get());
                    LocalDateTime validationTime = LocalDateTime.parse(selectable.xpath("//td[10]/text()").get(), DateTimeFormatter.ofPattern("yy-MM-dd HH:mm"));

                    if (speed < 5f && connTime < 5f && validationTime.plusMinutes(survivingTimeInMinutes).isAfter(now)) {
                        proxyIp.setSpeed(speed);
                        proxyIp.setConnTime(connTime);
                        proxyIp.setSurvivingTime(survivingTimeInMinutes);
                        proxyIp.setValidationTime(validationTime);
                        proxyIp.setIp(selectable.xpath("//td[2]/text()").get());
                        proxyIp.setIpPort(selectable.xpath("//td[3]/text()").get());
//                        proxyIp.setAddr(selectable.xpath("//td[4]/a/text()").get());
                        proxyIp.setAnonymity(selectable.xpath("//td[5]/text()").get().equals("高匿"));
                        proxyIp.setType(selectable.xpath("//td[6]/text()").get().equals("HTTP"));

                        proxyIps.add(proxyIp);
                    }
                }
            }
        }

        log.debug(page.getUrl() + "处理结束");
        return proxyIps;
    }

    @SneakyThrows
    @Override
    public void process(Page page) {
        Thread.sleep(new Random().nextInt(SLEEP_INTERVAL + SLEEP_INTERVAL / 2));
        if (page.getHtml() != null && pageCounter.getAndIncrement() < PAGE_LIMIT) {
            final List<String> targetPages = page.getHtml().xpath("//div[@class='pagination']/a[@class='next_page']").links().all();
            log.debug("pageCounter: {} currentPage: {} targetPages: {}", pageCounter.get(), page.getUrl(), targetPages.get(0));
            page.addTargetRequests(targetPages.subList(0, 1));
        }
        page.putField("items", fetchItems(page));
    }
}
