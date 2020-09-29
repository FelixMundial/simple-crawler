package com.example.webmagic.util;

import org.junit.jupiter.api.Test;

class ProxyUtilTest {
    @Test
    void validateIp() {
        System.out.println(ProxyUtil.validateIp("95.179.239.192:8080"));
    }
}