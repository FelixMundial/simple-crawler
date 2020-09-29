package com.example.webmagic.dao;

import com.example.webmagic.util.HttpUtil;
import com.example.webmagic.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.example.webmagic.constant.UrlConstant.VALIDATION_URL_BAIDU;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Repository
@Slf4j
public class ProxyRedisRepository {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("http://" + "${spring.redis.host}" + ":5555/random")
    private String proxyPoolUrl;

    /**
     * 调用/random接口从ProxyPool中直接获取一个可用IP，并进行可用性测试（同步）
     */
    public String getProxyString() {
        final LocalDateTime now = LocalDateTime.now();
        HttpResponse<String> response;
        String previousIp = "";

        /*
        若测试超过5分钟，则暂时放弃获取
         */
        do {
            response = HttpUtil.testHttpGetLocallyWithBody(proxyPoolUrl);
            if (response != null && response.statusCode() == HttpStatus.OK.value()
                    && !previousIp.equals(response.body()) && ProxyUtil.validateIp((previousIp = response.body()))) {
                return previousIp;
            }
        } while (LocalDateTime.now().minusMinutes(5).isBefore(now));
        log.warn("测试已超时，暂时放弃获取！");
        return null;
    }

    /**
     * 使用Redis接口直接从ProxyPool中获取多个可用IP，并进行可用性测试（异步）
     */
    public List<String> getProxyStringsByNative(int maxCount) {
        List<String> proxyStringList = new ArrayList<>();
        Set<String> rawIps;
        LocalTime now = LocalTime.now();
        int offset = 0;
        /*
        反复获取有效IP，直至成功获取或已等待超过5分钟
         */
        do {
            rawIps = redisTemplate.opsForZSet().reverseRangeByScore("proxies:universal", 10, 100, offset, maxCount);
            offset += maxCount;
            if (rawIps != null && rawIps.size() != 0) {
                /*
                异步测试
                 */
                proxyStringList = HttpUtil.fetchValidatedIpsAsync(VALIDATION_URL_BAIDU, new ArrayList<>(rawIps));
                log.debug("本批次IP测试结束，共{}个IP可用", proxyStringList.size());
                if (!proxyStringList.isEmpty()) {
                    break;
                }
            } else {
                /*
                无法使用Redis接口获取IP，不再重试
                 */
                log.warn("暂时无法使用Redis接口获取IP！");
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.debug("", e);
            }
        } while (LocalTime.now().minusMinutes(5).isBefore(now));
        return proxyStringList;
    }

    public List<String> getProxyStrings() {
        return getProxyStrings(50);
    }

    /**
     * 调用/random接口从ProxyPool中获取n个可用IP，不进行可用性测试
     */
    public List<String> getProxyStrings(int ipCount) {
        final LocalDateTime now = LocalDateTime.now();
        HttpResponse<String> response;
        String currentIp;
        String previousIp = "";
        List<String> ips = new LinkedList<>();

        for (int proxyCount = 0; proxyCount < ipCount && LocalDateTime.now().minusMinutes(5).isBefore(now); ) {
            response = HttpUtil.testHttpGetLocallyWithBody(proxyPoolUrl);
//            if (response != null && response.statusCode() == HttpStatus.OK.value()
//                    && ProxyUtil.validateIp(currentIp = response.body()) && !currentIp.equals(previousIp)) {
//                ips.add(currentIp);
//                proxyCount++;
//            }
            if (response != null && response.statusCode() == HttpStatus.OK.value()
                    && !(currentIp = response.body()).equals(previousIp)) {
                ips.add(currentIp);
                proxyCount++;
            }
        }
        return ips;
    }
}
