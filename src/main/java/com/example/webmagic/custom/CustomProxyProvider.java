package com.example.webmagic.custom;

import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
public class CustomProxyProvider extends SimpleProxyProvider {
    public CustomProxyProvider(List<Proxy> proxies) {
        super(proxies);
    }

    public static SimpleProxyProvider from(List<Proxy> proxies) {
        return new SimpleProxyProvider(Collections.unmodifiableList(proxies));
    }
}
