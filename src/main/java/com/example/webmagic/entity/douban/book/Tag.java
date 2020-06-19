package com.example.webmagic.entity.douban.book;

import lombok.Data;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Data
public class Tag {
    private String bookId;
    private Integer count;
    private String name;
    private String title;
}
