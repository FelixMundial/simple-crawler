package com.example.webmagic.util;

import com.example.webmagic.constant.UrlConstant;
import com.example.webmagic.entity.ProxyIp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.proxy.Proxy;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
@Slf4j
public class ProxyUtil {
    public static boolean validateIp(ProxyIp proxyIp) {
        return validateHttp(proxyIp.getIp(), proxyIp.getIpPort());
    }

    public static boolean validateIp(String fullIp) {
        final String[] ips = fullIp.split(":");
        return validateHttp(ips[0], ips[1]);
    }

    public static boolean validateHttp(String ip, String port) {
        return validateHttp("", ip, port);
    }

    /**
     * 测试代理IP
     *
     * @param url
     * @param ip
     * @param port
     * @return
     */
    public static boolean validateHttp(String url, String ip, String port) {
        boolean isAvailable = false;
        if (StringUtils.isEmpty(url)) {
            url = UrlConstant.VALIDATION_URL_BAIDU;
        } else {
            url = url.replaceFirst("https", "http");
        }

        final int statusCode = HttpUtil.testHttpGet(url, ip, port);
        if (statusCode == HttpStatus.OK.value()) {
            log.trace("{} ==> {}:{} Response:200", url, ip, port);
            isAvailable = true;
        } else {
            log.trace("{} ==> {}:{} Response:{}", url, ip, port, statusCode);
        }
        return isAvailable;
    }

    public static Proxy buildProxy(String fullIp) {
        final String[] ips = fullIp.split(":");
        return new Proxy(ips[0], Integer.parseInt(ips[1]));
    }
}
