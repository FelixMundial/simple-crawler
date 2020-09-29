package com.example.webmagic.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@SpringBootTest
@Slf4j
class RedisRepositoryTest {
    @Autowired
    private DoubanDoulistRedisRepository redisRepository;

    @Test
    void setBookId() {
        System.out.println(redisRepository.setFailedBookId("34920752"));
    }

    @Autowired
    ProxyRedisRepository proxyRedisRepository;

    @Test
    public void testFetchValidatedIpsAsync() {
        List<String> proxyStringList = proxyRedisRepository.getProxyStringsByNative(20);
//        List<String> proxyStringList = proxyRedisRepository.getProxyStrings(50);
//        proxyStringList = HttpUtil.fetchValidatedIpsAsync(VALIDATION_URL_BAIDU, proxyStringList);
        System.out.println(proxyStringList);
    }
}