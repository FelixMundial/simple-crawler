package com.example.webmagic.service;

import com.example.webmagic.dao.DoubanDoulistItemRedisRepository;
import com.example.webmagic.entity.douban.book.DoubanBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Service
public class DoubanBookService {
    @Autowired
    private DoubanDoulistItemRedisRepository redisRepository;
    @Autowired
    private DoubanApiService doubanApiService;

    /**
     * 将未成功获取信息的图书id暂存
     *
     * @param bookId
     * @return
     */
    public boolean registerFailedBook(String bookId) {
        return redisRepository.setFailedBookId(bookId)
                && redisRepository.addFailedRequest(bookId);
    }

    public boolean removeFailedBook(String bookId) {
        return redisRepository.removeFailedBookId(bookId);
    }

    /**
     * 将待获取信息的图书id暂存
     *
     * @param bookId
     * @return
     */
    public boolean registerBook(String bookId) {
        /*
        todo: 另存一份至MySQL或mq
         */
        return redisRepository.setBookId(bookId);
    }

    public String getFailedBookToProcess() {
        return redisRepository.getRandomFailedBookId();
    }

    public String getBookToProcess() {
        return redisRepository.getRandomBookId();
    }

    public DoubanBook getBookInfo(String bookId) {
        return doubanApiService.getDoubanBookInfo(bookId);
    }

    /**
     * 获取特定时间段内失败请求数
     */
    public int getFailedRequests() {
        Set<String> keys = redisRepository.getFailedKeys();
        if (keys != null) {
            return keys.size();
        }
        return 0;
    }
}
