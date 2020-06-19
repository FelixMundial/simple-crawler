package com.example.webmagic.entity.douban.book;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Data
public class Rating {
    private String bookId;
    @JsonAlias(value = "numRaters")
    private Integer numRaters;
    private Float average;
    private Float max;
    private Float min;
}
