package com.example.webmagic.entity.douban.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author yinfelix
 * @date 2020/6/20
 */
@Entity
@Table(name = "doulist_primary")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoubanDoulistItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "_id", nullable = false, unique = true)
    private Long id;
    @Column
    private Long bookId;
}
