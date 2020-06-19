package com.example.webmagic.service;

import com.example.webmagic.custom.CustomProxyProvider;
import com.example.webmagic.custom.EnhancedHttpClientDownloader;
import com.example.webmagic.dao.ProxyIpRepository;
import com.example.webmagic.dao.ProxyRedisRepository;
import com.example.webmagic.entity.ProxyIp;
import com.example.webmagic.util.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.proxy.Proxy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.webmagic.constant.SpiderConstant.ZONEID_ASIA_SHANGHAI;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
@Service
@Slf4j
public class ProxyService {
    @Autowired
    private ProxyRedisRepository proxyRedisRepository;
    @Autowired
    private ProxyService proxyService;

    public boolean refreshDownloaderProxy(EnhancedHttpClientDownloader downloader) {
        log.debug("尝试刷新代理...");
        Proxy proxy = proxyService.getAvailableProxyByProxyPool();
//        List<Proxy> proxies = proxyService.getAvailableProxiesByProxyPool();
        if (proxy != null) {
            downloader.setProxyProvider(CustomProxyProvider.from(proxy));
            log.debug("代理IP已刷新：" + proxy);
            return true;
        }
        return false;
    }

    public Proxy getAvailableProxyByProxyPool() {
        String ip = proxyRedisRepository.getProxyString();
        return StringUtils.isEmpty(ip) ? null : ProxyUtil.buildProxy(ip);
    }

    public List<Proxy> getAvailableProxiesByProxyPool() {
        final List<String> ips = proxyRedisRepository.getProxyStrings();
//        final List<String> ips = proxyRedisRepository.getProxyStrings0();
        return ips.isEmpty() ? null : ips.stream().map(ProxyUtil::buildProxy).collect(Collectors.toList());
    }

    //    @Autowired
    private ProxyIpRepository proxyIpRepository;

    /**
     * @return
     * @deprecated
     */
    public List<Proxy> getAvailableProxies() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZONEID_ASIA_SHANGHAI));
        List<ProxyIp> proxyIps = getProxyIps();
        List<ProxyIp> filteredProxyIps = new LinkedList<>();
        for (ProxyIp proxyIp : proxyIps) {
            boolean isAvailable = !proxyIp.getType() && proxyIp.getValidationTime().plusMinutes(proxyIp.getSurvivingTime()).isAfter(now);
            if (isAvailable) {
                isAvailable = ProxyUtil.validateIp(proxyIp);
                if (isAvailable) {
                    filteredProxyIps.add(proxyIp);
                } else {
                    proxyIpRepository.delete(proxyIp);
                }
            }
        }

        /*filteredProxyIps = proxyIps.stream().filter(
                proxyIp -> {
                    boolean isAvailable = *//*!proxyIp.getType() &&*//* proxyIp.getValidationTime().plusMinutes(proxyIp.getSurvivingTime()).isAfter(now) && ProxyUtil.validateIp(proxyIp);
                    if (!isAvailable) {
                        repository.delete(proxyIp);
                    }
                    return isAvailable;
                }).collect(Collectors.toList());*/

        if (!filteredProxyIps.isEmpty()) {
            return filteredProxyIps.stream().map(proxyIp -> new Proxy(proxyIp.getIp(), Integer.parseInt(proxyIp.getIpPort()))).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * @return
     * @deprecated
     */
    private List<ProxyIp> getProxyIps() {
        return proxyIpRepository.findAllByConnTimeLessThanEqualAndSpeedLessThanEqualOrderByConnTimeAscSpeedAsc(2f, 2f);
    }
}
