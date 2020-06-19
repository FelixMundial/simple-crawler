package com.example.webmagic.entity.zhihu;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.Map;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Data
public class ZhihuBillboardNode {
    @JsonAlias("cardId")
    private String cardId;
    @JsonAlias("feedSpecific")
    private Map feedSpecific;
    private ZhihuBillBoardTargetNode target;
}
