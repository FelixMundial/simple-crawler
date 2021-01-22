package com.example.webmagic.constant;

import java.util.Random;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
public class UrlConstant {
//    public static final String BASE_URL_BILIBILI_HOT = "https://www.bilibili.com/ranking/all/0/0/3";
public static final String BASE_URL_BILIBILI_HOT = "https://www.bilibili.com/v/popular/rank/all";
    public static final String BASE_URL_ZHIHU_HOT = "https://www.zhihu.com/billboard";
    public static final String BASE_URL_BAIDU_HOT = "http://top.baidu.com/buzz?b=1&fr=topindex";
    public static final String BASE_URL_WEIBO_HOT = "https://s.weibo.com/top/summary/";

    public static final String BASE_URL_XICIDAILI = "https://www.xicidaili.com/nn/";
    public static final String VALIDATION_URL_BAIDU = "http://www.baidu.com";
    public static final String VALIDATION_URL_BING = "http://www.bing.com";

    public static final String BASE_URL_DOULIST_PRIMARY = "https://m.douban.com/doulist/119825874/";
    public static final String BASE_URL_DOULIST_SECONDARY = "https://m.douban.com/doulist/119827658/";
    public static final String BASE_URL_DOULIST_TEST1 = "https://m.douban.com/doulist/225516/";
    public static final String BASE_URL_DOULIST_TEST2 = "https://m.douban.com/doulist/111168891/";
    public static final String BASE_URL_DOUBAN_API_V2 = "https://api.douban.com/v2/";

    public static final String[] DOUBAN_API_KEY = {"0b2bdeda43b5688921839c8ecb20399b", "0df993c66c0c636e29ecbb5344252a4a"};

    public static String getDoubanApiKey() {
        return DOUBAN_API_KEY[new Random().nextBoolean() ? 1 : 0];
    }
}
