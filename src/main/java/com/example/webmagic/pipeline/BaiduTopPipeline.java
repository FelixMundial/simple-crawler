package com.example.webmagic.pipeline;

import com.example.webmagic.dao.BaiduTopRepository;
import com.example.webmagic.entity.baidu.BaiduTopItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yinfelix
 * @date 2020/6/20
 */
@Component
@Slf4j
public class BaiduTopPipeline extends SimpleListPersistencePipeline<BaiduTopItem> {
    @Autowired
    private BaiduTopRepository repository;

    @Override
    public void processEach(BaiduTopItem baiduTopItem) {
        BaiduTopItem saveResult = repository.saveAndFlush(baiduTopItem);
        if (saveResult.getId() == null) {
            log.error("「" + baiduTopItem.getIKeyword() + "」暂时无法保存至数据库！");
        } else {
            log.debug("「" + baiduTopItem.getIKeyword() + "」已保存");
        }
    }
}
