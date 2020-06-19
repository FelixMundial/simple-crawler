package com.example.webmagic.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest
class RedisRepositoryTest {
    @Autowired
    private DoubanDoulistItemRedisRepository redisRepository;

    @Test
    void setBookId() {
        System.out.println(redisRepository.setFailedBookId("34920752"));
    }
}