package com.example.webmagic.pipeline;

import com.example.webmagic.service.DoubanBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Component
@Slf4j
public class DoubanDoulistPipeline extends SimpleListPersistencePipeline<String> {
    @Autowired
    private DoubanBookService bookService;

    @Override
    public void processEach(String bookId) {
        boolean operationResult;
        /*
        todo: 此处根据是否在数据库中已连续存在该书籍id决定是否提前中止整个爬取
         */
        if (bookId.matches("^\\d{1,}$")) {
            do {
                operationResult = bookService.registerBook(bookId);
            } while (!operationResult);
        }
    }
}
