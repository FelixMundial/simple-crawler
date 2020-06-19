package com.example.webmagic.entity.douban.book;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Data
public class DoubanBook implements Serializable {
    private String id;
    private String title;
    private String subtitle;
    @JsonIgnore
    private LocalDate pubdate;
    @JsonAlias("pubdate")
    private String rawPubdate;
    @JsonAlias(value = "origin_title")
    private String originalTitle;
    private String image;
    private String binding;
    private String catalog;
    private Integer pages;
    private String alt;
    private String publisher;
    private String isbn10;
    private String isbn13;
    private String url;
    private String altTitle;
    private String authorIntro;
    private String summary;
    private String price;
    @JsonAlias("author")
    private List<String> authors;
    @JsonAlias("translator")
    private List<String> translators;
    private Series series;
    private List<Tag> tags;
    private Rating rating;
    private LocalDate updateTime;
}
