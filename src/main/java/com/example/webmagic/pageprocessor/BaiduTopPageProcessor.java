package com.example.webmagic.pageprocessor;

import com.example.webmagic.entity.baidu.BaiduTopItem;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;

import java.util.Collection;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Slf4j
public class BaiduTopPageProcessor extends SimpleListPageProcessor<BaiduTopItem> {
    @Override
    public Collection<BaiduTopItem> fetchItems(Page page) {
        return null;
    }
}
