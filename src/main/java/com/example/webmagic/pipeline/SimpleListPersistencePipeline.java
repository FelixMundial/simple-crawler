package com.example.webmagic.pipeline;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
@Slf4j
public abstract class SimpleListPersistencePipeline<T> implements Pipeline {
    /**
     * 单个实体类对象的持久化逻辑
     *
     * @param t 单个实体类对象
     */
    public abstract void processEach(T t);

    @Override
    public void process(ResultItems resultItems, Task task) {
        if (resultItems.getAll() != null && resultItems.getAll().size() > 0) {
            List<T> items = (List<T>) resultItems.getAll().values().stream().findFirst().get();
            items.forEach(this::processEach);
            log.info("{}集合持久化结束，共保存{}条记录", items.get(0).getClass().getSimpleName(), items.size());
        } else {
            log.error("暂未爬取到任何实体类对象，无法持久化！");
        }
    }
}
