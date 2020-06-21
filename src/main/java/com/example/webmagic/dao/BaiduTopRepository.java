package com.example.webmagic.dao;

import com.example.webmagic.entity.baidu.BaiduTopItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yinfelix
 * @date 2020/6/20
 */
public interface BaiduTopRepository extends JpaRepository<BaiduTopItem, Long> {
}
