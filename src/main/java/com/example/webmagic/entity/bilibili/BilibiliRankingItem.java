package com.example.webmagic.entity.bilibili;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yinfelix
 * @date 2020/6/15
 */
@Entity
@Table(name = "bilibili_hot")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BilibiliRankingItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column
    private String rankingNumber;
    @Column
    private String vImageUrl;
    @Column
    private String vTitle;
    @Column
    private String vBv;
    @Column
    private String vPlayCount;
    @Column
    private String vViewCount;
    @Column
    private String vAuthor;
    @Column
    private String vAuthorUrl;
    @Column
    private String vPoints;
    @Column
    private String vDesc;
    @Column
    private LocalDateTime updateTime;
}
