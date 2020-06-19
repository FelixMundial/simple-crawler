package com.example.webmagic.util;

import com.example.webmagic.service.DoubanApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest
class HttpUtilTest {
    @Autowired
    private DoubanApiService doubanApiService;

    @Test
    void testHttpGet0() {
        System.out.println(doubanApiService.getDoubanBookInfo("34920752"));
    }
}