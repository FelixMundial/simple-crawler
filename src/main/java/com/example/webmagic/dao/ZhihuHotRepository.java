package com.example.webmagic.dao;

import com.example.webmagic.entity.zhihu.ZhihuHotItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
public interface ZhihuHotRepository extends JpaRepository<ZhihuHotItem, Long> {
}
