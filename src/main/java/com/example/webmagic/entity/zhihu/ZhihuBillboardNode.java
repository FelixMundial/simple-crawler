package com.example.webmagic.entity.zhihu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Data
public class ZhihuBillboardNode {
    @JsonProperty("cardId")
    private String cardId;
    @JsonProperty("feedSpecific")
    private Map feedSpecific;
    private ZhihuBillBoardTargetNode target;
}
