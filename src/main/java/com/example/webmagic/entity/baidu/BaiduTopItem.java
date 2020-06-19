package com.example.webmagic.entity.baidu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yinfelix
 * @date 2020/6/19
 */
@Entity
@Table(name = "baidu_hot")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaiduTopItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column
    private String rankingNumber;
    @Column
    private String iKeyword;
    @Column
    private String iTitle;
    @Column
    private String iText;
    @Column
    private String iNewsUrl;
    @Column
    private String iVideoUrl;
    @Column
    private String iPictureUrl;
    @Column
    private String iMetrics;
    @Column
    private LocalDateTime updateTime;
}
