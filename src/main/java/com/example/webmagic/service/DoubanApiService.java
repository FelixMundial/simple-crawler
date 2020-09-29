package com.example.webmagic.service;

import com.example.webmagic.constant.UrlConstant;
import com.example.webmagic.entity.douban.book.DoubanBook;
import com.example.webmagic.util.HttpUtil;
import com.example.webmagic.util.TimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;

import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Slf4j
@Service
public class DoubanApiService {
    public static final String DOUBAN_DATE_PATTERN = "yyyy-M-d";
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 借助豆瓣api获取图书信息
     *
     * @param bookId
     * @return
     */
    public DoubanBook getDoubanBookInfo(String bookId) {
        final HttpResponse<String> httpResponse = HttpUtil.testHttpGetLocallyWithBody(UrlConstant.BASE_URL_DOUBAN_API_V2
                + "book/" + bookId + "?apiKey=" + UrlConstant.getDoubanApiKey());
        log.debug("id:{} requested:{}", bookId, httpResponse.statusCode());
        final String text;
        if (httpResponse.statusCode() == HttpStatus.OK.value()) {
            text = httpResponse.body();
            DoubanBook doubanBook = null;
            try {
                doubanBook = objectMapper.readValue(text, DoubanBook.class);
                String rawPubdate = doubanBook.getRawPubdate();
                doubanBook.setPubdate(TimeUtil.parseDateString(rawPubdate));
                doubanBook.setUpdateTime(LocalDate.now(ZoneId.of(ZONEID_ASIA_SHANGHAI)));
            } catch (JsonProcessingException e) {
                log.error("id:{}解析错误", bookId, e);
            }
            return doubanBook;
        }
        log.error("豆瓣api请求失败：{}", httpResponse.body());
        /*
        todo: 若apiKey异常，直接进行邮件提醒
         */
        return null;
    }
}
