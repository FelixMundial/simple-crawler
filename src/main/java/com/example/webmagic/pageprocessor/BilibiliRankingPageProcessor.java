package com.example.webmagic.pageprocessor;

import com.example.webmagic.entity.bilibili.BilibiliRankingItem;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.webmagic.constant.SpiderConstant.INITIAL_CAPACITY;
import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@Slf4j
public class BilibiliRankingPageProcessor extends SimpleListPageProcessor<BilibiliRankingItem> {
    @Override
    public Collection<BilibiliRankingItem> fetchItems(Page page) {
        List<BilibiliRankingItem> rankingItems = null;

        log.debug("开始处理{}页面", page.getUrl());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));

        if (page.getHtml() != null) {
            int itemCount = 0;
            List<Selectable> selectables = page.getHtml().css("li.rank-item").nodes();

            if (selectables != null && selectables.size() > 0) {
                rankingItems = new ArrayList<>(INITIAL_CAPACITY);

                for (Selectable selectable : selectables) {
                    BilibiliRankingItem rankingItem = new BilibiliRankingItem();

                    String ranking = selectable.xpath("//div[@class='num']/text()").get();
                    rankingItem.setRankingNumber(ranking);

                    Selectable content = selectable.css("div.content");
                    if (content.match()) {
                        String imageUrl = content.xpath("//div[@class='lazy-img cover']/@src").get();
                        String title = content.xpath("//div[@class='lazy-img cover']/img/@alt").get();
                        rankingItem.setVImageUrl(imageUrl);
                        rankingItem.setVTitle(title);

                        Selectable info = content.css("div.info");
                        if (info.match()) {
                            String url = info.xpath("//a[@class='title']/@href").get();
                            rankingItem.setVBv(url.split("/")[4]);

                            Selectable detail = info.css("div.detail");
                            if (detail.match()) {
                                List<String> dataBoxes = detail.xpath("//span[@class='data-box']/text()").all();
                                String authorUrl = detail.xpath("//a[@target='_blank']/@href").get();
                                String points = info.xpath("//div[@class='pts']/div/text()").get();
                                rankingItem.setVPlayCount(dataBoxes.get(0));
                                rankingItem.setVViewCount(dataBoxes.get(1));
                                rankingItem.setVAuthor(dataBoxes.get(2));
                                rankingItem.setVAuthorUrl(authorUrl.substring(2));
                                rankingItem.setVPoints(points);
                                rankingItem.setUpdateTime(now);
                            }
                        }
                    }

                    rankingItems.add(rankingItem);
                    if (++itemCount == INITIAL_CAPACITY) {
                        break;
                    }
                }
            }
        }

        log.debug("页面{}处理结束", page.getUrl());
        return rankingItems;
    }
}
