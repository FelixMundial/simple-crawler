package com.example.webmagic.pipeline;

import com.example.webmagic.dao.BilibiliRankingRepository;
import com.example.webmagic.entity.bilibili.BilibiliRankingItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@Component
@Slf4j
public class BilibiliRankingPipeline extends SimpleListPersistencePipeline<BilibiliRankingItem> {
    @Autowired
    private BilibiliRankingRepository repository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processEach(BilibiliRankingItem bilibiliRankingItem) {
        BilibiliRankingItem saveResult = repository.saveAndFlush(bilibiliRankingItem);
        if (saveResult.getId() == null) {
            log.error(bilibiliRankingItem.getVBv() + "暂时无法保存至数据库！");
        } else {
//            log.debug(bilibiliRankingItem.getVBv() + "已保存");
        }
    }

    @Override
    public void processIllegalData(List<BilibiliRankingItem> items, Throwable e) {

    }
}
