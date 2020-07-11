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
import java.util.*;

import static com.example.webmagic.constant.SpiderConstant.PROXY_SIZE;

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
     * 调用/random接口从ProxyPool中直接获取一个可用IP，并进行额外测试
     *
     * @return
     */
    public String getProxyString() {
        final LocalDateTime now = LocalDateTime.now();
        HttpResponse<String> response;
        String previousIp = "";

        /*
        若测试超过5分钟，则暂时放弃继续获取
         */
        while (LocalDateTime.now().minusMinutes(5).isBefore(now)) {
            response = HttpUtil.testHttpGet0(proxyPoolUrl);
            if (response != null && response.statusCode() == HttpStatus.OK.value()
                    && !previousIp.equals(response.body()) && ProxyUtil.validateIp((previousIp = response.body()))) {
                return previousIp;
            }
        }
        log.warn("测试已超时，暂时放弃继续获取！");
        return null;
    }

    /**
     * 调用/random接口从ProxyPool中直接获取多个可用IP
     *
     * @return
     */
    public List<String> getProxyStrings() {
        final LocalDateTime now = LocalDateTime.now();
        HttpResponse<String> response;
        String currentIp;
        String previousIp = "";
        List<String> ips = new LinkedList<>();

        /*
        若测试超过5分钟，则放弃继续获取
         */
        for (int proxyCount = 0; proxyCount < PROXY_SIZE && LocalDateTime.now().minusMinutes(5).isBefore(now); ) {
            response = HttpUtil.testHttpGet0(proxyPoolUrl);
            /*if (response != null && response.statusCode() == HttpStatus.OK.value()
                    && ProxyUtil.validateIp(currentIp = response.body()) && !currentIp.equals(previousIp)) {
                ips.add(currentIp);
                proxyCount++;
            }*/
            if (response != null && response.statusCode() == HttpStatus.OK.value()
                    && !(currentIp = response.body()).equals(previousIp)) {
                ips.add(currentIp);
                proxyCount++;
            }
        }
        return ips;
    }

    /**
     * 从ProxyPool中直接获取多个可用IP
     *
     * @return
     */
    public List<String> getProxyStrings0() {
        final LocalDateTime now = LocalDateTime.now();
        int proxyCount;
        Set<String> resultIps = new HashSet<>();
        Set<String> rawIps;
        do {
            rawIps = redisTemplate.opsForZSet().reverseRangeByScore("proxies:universal", 98, 100, 0, PROXY_SIZE);
            resultIps.addAll(Objects.requireNonNull(rawIps));
//            if (!resultIps.isEmpty()) {
//                resultIps.removeIf(ipString -> !ProxyUtil.validateIp(ipString));
//            }
            proxyCount = resultIps.size();
        } while (proxyCount < PROXY_SIZE && LocalDateTime.now().minusMinutes(15).isBefore(now));
        return new ArrayList<>(resultIps);
    }
}
