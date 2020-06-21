package com.example.webmagic.dao;

import com.example.webmagic.entity.douban.book.DoubanDoulistItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
public interface DoubanDoulistRepository extends JpaRepository<DoubanDoulistItem, Long> {
}
