package com.example.webmagic.pageprocessor;

import com.example.webmagic.constant.SpiderConstant;
import com.example.webmagic.entity.baidu.BaiduTopItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Component
@Slf4j
public class BaiduTopPageProcessor extends SimpleListPageProcessor<BaiduTopItem> {
    public static final int RANKING_ITEM_COUNT = 20;

    public BaiduTopPageProcessor() {
        setSiteCharset(SpiderConstant.CHARSET_GB2312);
    }

    @Override
    public Collection<BaiduTopItem> fetchItems(Page page) {
        List<BaiduTopItem> topItems = new ArrayList<>();

        log.debug("开始处理{}页面", page.getUrl());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));

        try {
            if (page.getHtml() != null) {
                List<Selectable> selectables = page.getHtml().xpath("//table[@class='list-table']/tbody/tr").nodes();

                if (selectables != null && selectables.size() > 0) {
                    int size = selectables.size();
                    int itemCount = 0;
                    topItems = new ArrayList<>(size);

                    for (int i = 0; itemCount < RANKING_ITEM_COUNT; i++) {
                        Selectable currentSelectable = selectables.get(i);
                        String keyword = currentSelectable.xpath("//td[@class='keyword']/a/text()").get();
                        if (!StringUtils.isEmpty(keyword)) {
                            BaiduTopItem topItem = new BaiduTopItem();
                            topItem.setIKeyword(keyword);
                            String rankingNum = currentSelectable.xpath("//td[@class='first']/span/text()").get();
                            topItem.setRankingNumber(rankingNum);

                            if (currentSelectable.xpath("//tr[@class='hideline']").match()) {
                                if (selectables.get(i + 1).xpath("//tr[@class='item-tr']").match()) {
                                    topItem.setITitle(selectables.get(i + 1).xpath("//tr[@class='item-tr']/td/div/div/a/text()").get());
                                    topItem.setIText(selectables.get(i + 1).xpath("//tr[@class='item-tr']/td/div/div/p/text()").get());
                                    i++;
                                }
                            }

                            String metrics = currentSelectable.xpath("//td[@class='last']/span/text()").get();
                            topItem.setIMetrics(metrics);
                            topItem.setUpdateTime(now);

                            topItems.add(topItem);
                            itemCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            page.setSkip(true);
            page.setDownloadSuccess(false);
            return null;
        }

        log.debug("页面{}处理结束", page.getUrl());
        return topItems;
    }
}
