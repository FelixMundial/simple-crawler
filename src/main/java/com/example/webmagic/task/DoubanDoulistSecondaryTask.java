package com.example.webmagic.task;

import com.example.webmagic.entity.douban.book.DoubanBook;
import com.example.webmagic.service.DoubanBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Component
@Slf4j
@EnableScheduling
public class DoubanDoulistSecondaryTask {
    public static final int FAILED_REQ_THRESHOLD = 5;

    private final AtomicBoolean isRequestAllowed = new AtomicBoolean(true);

    @Autowired
    private DoubanBookService bookService;

    /*
    todo: 调用api失败或写入数据库失败的书籍id分别存入zset（score值为失败次数），
     并在额外的任务中随机获取元素进行重试，成功则移除，失败则更新score值
     */
    @Async
    @Scheduled(cron = "0 */5 * * * *")
    public void initDoubanApiTask() throws InterruptedException {
        boolean isTaskCompleted = false;
        log.info("initDoubanApiTask()");
        if (isRequestAllowed.get()) {
            String bookId = bookService.getBookToProcess();
            /*
            是否存在需要处理的书籍；获取的字符串是否符合图书id格式
             */
            if (!StringUtils.isEmpty(bookId) && bookId.matches("^\\d{1,}$")) {
                DoubanBook bookInfo = bookService.getBookInfo(bookId);
                /*
                是否成功获取图书信息
                 */
                if (bookInfo != null) {
                    /*
                    todo: DB操作，若失败也加入failedBook
                     */
                    isTaskCompleted = true;
                } else {
                    boolean result;
                    do {
                        result = bookService.registerFailedBook(bookId);
                    } while (!result);
                }
            }
            Thread.sleep(5 * 60 * 1000);
            if (isTaskCompleted && isRequestAllowed.get()) {
                bookId = bookService.getFailedBookToProcess();
                /*
                是否存在需要重新处理的书籍
                 */
                if (!StringUtils.isEmpty(bookId)) {
                    DoubanBook bookInfo = bookService.getBookInfo(bookId);
                    /*
                    是否成功获取图书信息
                     */
                    if (bookInfo != null) {
                    /*
                    todo: DB操作，若成功则从failedBook中移除
                     */
                    } else {
                        boolean result;
                        do {
                            result = bookService.registerFailedBook(bookId);
                        } while (!result);
                    }
                }
            }
        }
    }

    /**
     * 每十分钟监测该时间段内失败请求频率，以决定是否暂停api请求
     */
    @Async
    @Scheduled(cron = "0 */30 * * * *")
    public void initRequestBrakeCheck() {
        int failedRequests = bookService.getFailedRequests();
        if (failedRequests > FAILED_REQ_THRESHOLD) {
            isRequestAllowed.set(false);
            log.error("触发api熔断！");
        }
    }
}
