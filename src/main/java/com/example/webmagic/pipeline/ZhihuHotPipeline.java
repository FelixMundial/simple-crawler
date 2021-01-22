package com.example.webmagic.pipeline;

import com.example.webmagic.dao.ZhihuHotRepository;
import com.example.webmagic.entity.zhihu.ZhihuHotItem;
import com.example.webmagic.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Component
@Slf4j
public class ZhihuHotPipeline extends SimpleListPersistencePipeline<ZhihuHotItem> {
    @Autowired
    private ZhihuHotRepository repository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processEach(ZhihuHotItem zhihuHotItem) {
//        System.out.println(zhihuHotItem.getQTitle());
        ZhihuHotItem saveResult = repository.saveAndFlush(zhihuHotItem);
        if (saveResult.getId() == null) {
            throw new RuntimeException(zhihuHotItem.getQId() + "暂时无法保存至数据库！");
        } /*else {
            log.debug(zhihuHotItem.getQId() + "已保存");
        }*/
    }

    @Override
    public void processIllegalData(List<ZhihuHotItem> items, Throwable ex) {
        try {
            LocalDateTime updateTime = items.get(0).getUpdateTime();

            String itemsKey = "webmagic:pipeline:" + updateTime;
            if (ex.getCause() instanceof DataException) {
                log.error(itemsKey + ": " + ((DataException) ex.getCause()).getSQLException().getMessage());
            }

            /*
            TODO 结合延时队列与定时任务实现错误消息回查
             */
            rabbitTemplate.convertAndSend("exchange", "routingKey", itemsKey);
            String parsedItems = JsonUtil.parse(items);
            /*
            TODO 使用MongoDB代替Redis存储大型key
             */
            redisTemplate.boundValueOps(itemsKey).set(parsedItems);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
