package com.example.webmagic.dao;

import com.example.webmagic.entity.ProxyIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author yinfelix
 * @date 2020/6/16
 * @deprecated
 */
public interface ProxyIpRepository extends JpaRepository<ProxyIp, Long> {
    ProxyIp findByIpAndIpPort(String ip, String ipPort);

    List<ProxyIp> findAllByConnTimeLessThanEqualAndSpeedLessThanEqualOrderByConnTimeAscSpeedAsc(Float connTime, Float speed);
}
