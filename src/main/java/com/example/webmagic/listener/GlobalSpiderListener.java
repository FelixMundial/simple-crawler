package com.example.webmagic.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

/**
 * @author yinfelix
 * @date 2020/9/11
 */
@Slf4j
@Component
public class GlobalSpiderListener implements SpiderListener {
    @Override
    public void onSuccess(Request request) {

    }

    @Override
    public void onError(Request request) {
        log.error("发生系统异常：{}", request);
    }
}
