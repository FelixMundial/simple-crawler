package com.example.webmagic.pageprocessor;

import com.example.webmagic.custom.EnhancedHttpClientDownloader;
import com.example.webmagic.entity.zhihu.ZhihuBillboardNode;
import com.example.webmagic.entity.zhihu.ZhihuHotItem;
import com.example.webmagic.service.ProxyService;
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
    public static final int RANKING_ITEM_COUNT = 20;

    @Autowired
    private EnhancedHttpClientDownloader downloader;
    @Autowired
    private ProxyService proxyService;
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Collection<ZhihuHotItem> fetchItems(Page page) {
        List<ZhihuHotItem> hotItems;

        log.debug("开始处理{}页面", page.getUrl());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));

        try {
            final String rawText = page.getRawText();
            checkContent(rawText);
            String hotListString = getHotListString(rawText);
            final List<ZhihuBillboardNode> nodes = Arrays.asList(objectMapper.readValue(hotListString, ZhihuBillboardNode[].class));
            hotItems = new LinkedList<>();
            final ListIterator<ZhihuBillboardNode> nodeListIterator = nodes.listIterator();
            int index = 0;
            boolean isStringLogged = false;

            while (nodeListIterator.hasNext() && index < RANKING_ITEM_COUNT) {
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
                if (metricsArea != null && metricsArea.contains("万热度") && metricsArea.length() < 24) {
                    item.setQMetrics(metricsArea);
                } else {
                    if (!isStringLogged) {
                        log.warn("JSON数据metric字段出现异常：{}，所在排名：{}", node, index);
                        /*
                        TODO 执行邮件提醒并将异常数据缓存至Redis
                         */
                        isStringLogged = true;
                    }
                    item.setQMetrics("");
                }
                String imageArea = node.getTarget().getImageArea().get("url").toString();
                item.setQImageUrl(imageArea);
                String feedSpecific = node.getFeedSpecific().get("answerCount").toString();
                item.setQAnswersCount(Integer.parseInt(feedSpecific));
                item.setUpdateTime(now);
                hotItems.add(item);
            }
        } catch (Throwable e) {
            /*
            跳过后续pipeline，刷新代理并进行循环重试
             */
            page.setSkip(true);
            page.setDownloadSuccess(false);
            proxyService.refreshDownloaderProxy(downloader);
            return null;
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
            throw new IllegalArgumentException("数据为空！");
        }
    }

    /**
     * 对JSON字符串进行格式检测
     */
    private String getHotListString(String rawText) {
        Stack<Character> stack = new Stack<>();
        final int hotListIndex = rawText.indexOf("\"hotList\":");
        if (hotListIndex != -1) {
            int startIndex = rawText.indexOf("[", hotListIndex);
            if (startIndex != -1) {
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
                if (endIndex >= rawText.length()) {
                    if (!stack.empty()) {
                        log.error("JSON数据异常：共{}个中括号不匹配！", stack.size());
                    }
                    /*
                    todo: 执行邮件提醒并将异常数据缓存至Redis
                     */
                    log.error("JSON数据异常：endIndex >= rawText.length()，且startIndex为{}\n异常JSON为{}", startIndex, rawText);
                    throw new IllegalArgumentException("JSON数据异常");
                }
                return rawText.substring(startIndex, ++endIndex);
            }
        }
        if (!rawText.contains("系统监测到您的网络环境存在异常，为保证您的正常访问，请输入验证码进行验证")) {
            log.error("网页数据错误，找不到hotList字段！\n异常JSON为{}", ""/*rawText*/);
        }
        throw new RuntimeException("原始JSON数据异常");
    }
}
