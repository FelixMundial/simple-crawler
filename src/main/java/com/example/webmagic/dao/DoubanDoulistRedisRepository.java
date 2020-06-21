package com.example.webmagic.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Repository
@Slf4j
public class DoubanDoulistRedisRepository {
    public static final String DOULIST_SET_KEY = "doulist:book:todo:id";
    public static final String DOULIST_ZSET_KEY = "doulist:book:failed:id";
    public static final String DOULIST_STRING_KEY = "failedReq:id:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 将未成功获取信息的图书id暂存至ZSet
     *
     * @param bookId
     * @return
     */
    public boolean setFailedBookId(String bookId) {
        Double res = null;
        try {
            res = redisTemplate.opsForZSet().incrementScore(DOULIST_ZSET_KEY, bookId, 1);
        } catch (Exception e) {
            log.error("setFailedBookId()", e);
        }
        return res != null;
    }

    /**
     * 将ZSet中未成功获取信息的图书id移除
     *
     * @param bookId
     * @return
     */
    public boolean removeFailedBookId(String bookId) {
        Long res = redisTemplate.opsForZSet().remove(DOULIST_ZSET_KEY, bookId);
        return res > 0;
    }

    /**
     * 将待获取信息的图书id暂存至Set
     *
     * @param bookId
     * @return
     */
    public boolean setBookId(String bookId) {
        Long res = null;
        try {
            res = redisTemplate.opsForSet().add(DOULIST_SET_KEY, bookId);
        } catch (Exception e) {
            log.error("setBookId()", e);
        }
        return res != null;
    }

    public String getRandomBookId() {
        return redisTemplate.opsForSet().pop(DOULIST_SET_KEY);
    }

    public String getRandomFailedBookId() {
        Long count = redisTemplate.opsForZSet().zCard(DOULIST_ZSET_KEY);
        /*
        此处count值不会超过Integer上限
         */
        if (count != null && count > 0) {
            int randomIndex = new Random().nextInt(Math.toIntExact(count));
            Set<String> strings = redisTemplate.opsForZSet().rangeByScore(DOULIST_ZSET_KEY, randomIndex, randomIndex);
            if (strings != null && !strings.isEmpty()) {
                return strings.iterator().next();
            }
        }
        return null;
    }

    /**
     * 将未成功获取信息的图书id暂存至String，过期时间三十分钟
     *
     * @param bookId
     * @return
     */
    public boolean addFailedRequest(String bookId) {
        try {
            redisTemplate.opsForValue().set(DOULIST_STRING_KEY + bookId, "", 30, TimeUnit.MINUTES);
            String value = redisTemplate.opsForValue().get(DOULIST_STRING_KEY + bookId);
            return !StringUtils.isEmpty(value);
        } catch (Exception e) {
            log.error("addFailedRequest()", e);
        }
        return false;
    }

    public Set<String> getFailedKeys() {
        try {
            return redisTemplate.keys("DOULIST_STRING_KEY + \"*\"");
        } catch (Exception e) {
            log.error("getFailedKeys()", e);
        }
        return null;
    }
}
