package com.example.webmagic.dao;

import com.example.webmagic.entity.bilibili.BilibiliRankingItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
public interface BilibiliRankingRepository extends JpaRepository<BilibiliRankingItem, Long> {
}
