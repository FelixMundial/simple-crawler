package com.example.webmagic.pageprocessor;

import com.example.webmagic.entity.zhihu.ZhihuBillboardNode;
import com.example.webmagic.entity.zhihu.ZhihuHotItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Component
@Slf4j
public class ZhihuHotPageProcessor extends SimpleListPageProcessor<ZhihuHotItem> {
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Collection<ZhihuHotItem> fetchItems(Page page) {
        List<ZhihuHotItem> hotItems = null;

        log.debug("开始处理{}页面", page.getUrl());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));

        final String rawText = page.getRawText();
        checkContent(rawText);
        String hotListString = getHotListString(rawText);
        try {
            checkContent(hotListString);
            final List<ZhihuBillboardNode> nodes = Arrays.asList(objectMapper.readValue(hotListString, ZhihuBillboardNode[].class));
            hotItems = new LinkedList<>();
            final ListIterator<ZhihuBillboardNode> nodeListIterator = nodes.listIterator();
            int index = 0;
            while (nodeListIterator.hasNext() && index < 20) {
                index = nodeListIterator.nextIndex() + 1;
                ZhihuBillboardNode node = nodeListIterator.next();
                ZhihuHotItem item = new ZhihuHotItem();
                item.setQId(node.getCardId().replaceFirst("Q_", ""));
                item.setRankingNumber(index + "");
                String titleArea = node.getTarget().getTitleArea().get("text").toString();
                item.setQTitle(titleArea);
                String excerptArea = node.getTarget().getExcerptArea().get("text").toString();
                item.setQExcerpt(excerptArea);
                String metricsArea = node.getTarget().getMetricsArea().get("text").toString();
                item.setQMetrics(metricsArea);
                String imageArea = node.getTarget().getImageArea().get("url").toString();
                item.setQImageUrl(imageArea);
                String feedSpecific = node.getFeedSpecific().get("answerCount").toString();
                item.setQAnswersCount(Integer.parseInt(feedSpecific));
                item.setUpdateTime(now);
                hotItems.add(item);
            }
        } catch (Exception e) {
            log.error("JSON解析错误：", e);
            /*
            todo: 执行邮件提醒任务通知该出错数据
             */
            log.error(hotListString);
            /*
            后续pipeline跳过该出错数据，直接进行cycle-retry
             */
            page.setSkip(true);
            page.setDownloadSuccess(false);
            Thread.sleep(60 * 1000);
        }

        /*if (page.getHtml() != null) {
            int itemCount = 0;
            List<Selectable> selectables = page.getHtml().css("div.HotList-list").nodes();

            if (selectables != null && selectables.size() > 0) {
                hotItems = new ArrayList<>(INITIAL_CAPACITY);

                for (Selectable selectable : selectables) {
                    ZhihuHotItem hotItem = new ZhihuHotItem();

                    String ranking = selectable.xpath("//div[@class='HotItem-index']/div/text()").get();
                    hotItem.setRankingNumber(ranking);

                    Selectable content = selectable.css("div.HotItem-content");
                    if (content.match()) {
                        String imageUrl = selectable.xpath("//div[@class='HotItem-img']/img/@src").get();
                        String title = content.xpath("//a/h2[@class='HotItem-title']/text()").get();
                        String excerpt = content.xpath("//a/p[@class='HotItem-excerpt']/text()").get();
                        hotItem.setQImageUrl(imageUrl);
                        hotItem.setQTitle(title);
                        hotItem.setQExcerpt(excerpt);
                        String url = content.xpath("//a/@href").get();
                        hotItem.setQId(url.split("/")[4]);
                        String metrics = content.xpath("//div[@class='HotItem-metrics']/text()").get();
                        hotItem.setQMetrics(metrics);
                        hotItem.setUpdateTime(now);
                    }

                    hotItems.add(hotItem);
                    if (++itemCount == INITIAL_CAPACITY) {
                        break;
                    }
                }
            }
        }*/

        log.debug("页面{}处理结束", page.getUrl());
        return hotItems;
    }

    private void checkContent(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new RuntimeException("数据为空！");
        }
    }

    /**
     * 对json字符串进行括号匹配检测
     *
     * @param rawText
     * @return
     */
    private String getHotListString(String rawText) {
        Stack<Character> stack = new Stack<>();
        final int hotListIndex = rawText.indexOf("\"hotList\":");
        final int startIndex = rawText.indexOf("[", hotListIndex);
        int endIndex;
        for (endIndex = startIndex; endIndex < rawText.length(); endIndex++) {
            if (rawText.charAt(endIndex) == '[') {
                stack.push('[');
            }
            if (rawText.charAt(endIndex) == ']') {
                stack.pop();
                if (stack.empty() && rawText.indexOf("],\"guestFeeds\":") == endIndex) {
                    break;
                }
            }
        }
        String hotListString = rawText.substring(startIndex, endIndex + 1);

        if (!stack.empty()) {
            hotListString += "]";
        }
        return hotListString;
    }
}
