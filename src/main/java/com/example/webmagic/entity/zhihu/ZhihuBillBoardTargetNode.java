package com.example.webmagic.entity.zhihu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Data
public class ZhihuBillBoardTargetNode {
    @JsonProperty("titleArea")
    private Map titleArea;
    @JsonProperty("excerptArea")
    private Map excerptArea;
    @JsonProperty("imageArea")
    private Map imageArea;
    @JsonProperty("metricsArea")
    private Map metricsArea;
    private Map link;
}
