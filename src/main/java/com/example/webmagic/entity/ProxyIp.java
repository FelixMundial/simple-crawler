package com.example.webmagic.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
@Entity
@Table(name = "proxy_ip")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyIp implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column
    private String ip;
    @Column
    private String ipPort;
    @Column
    private String addr;
    @Column
    private Boolean anonymity;
    @Column
    private Boolean type;
    @Column
    private Float speed;
    @Column
    private Float connTime;
    @Column
    private Long survivingTime;
    @Column
    private LocalDateTime validationTime;
}
