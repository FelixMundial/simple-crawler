package com.example.webmagic.entity.zhihu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Entity
@Table(name = "zhihu_hot")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhihuHotItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column
    private String rankingNumber;
    @Column
    private String qImageUrl;
    @Column
    private String qTitle;
    @Column
    private String qId;
    @Column
    private String qMetrics;
    @Column
    private String qExcerpt;
    @Column
    private Integer qAnswersCount;
    @Column
    private LocalDateTime updateTime;
}
