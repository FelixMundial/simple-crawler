package com.example.webmagic.entity.zhihu;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.Map;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Data
public class ZhihuBillBoardTargetNode {
    @JsonAlias("titleArea")
    private Map titleArea;
    @JsonAlias("excerptArea")
    private Map excerptArea;
    @JsonAlias("imageArea")
    private Map imageArea;
    @JsonAlias("metricsArea")
    private Map metricsArea;
    private Map link;
}
