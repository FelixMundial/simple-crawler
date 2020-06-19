package com.example.webmagic.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest
class ProxyUtilTest {
    @Test
    void validateIp() {
        System.out.println(ProxyUtil.validateIp("95.179.239.192:8080"));
    }
}