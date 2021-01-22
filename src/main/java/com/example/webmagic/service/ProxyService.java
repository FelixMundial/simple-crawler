package com.example.webmagic.service;

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
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

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
//        Proxy proxy = proxyService.getAvailableProxyByProxyPool();
//        if (proxy != null) {
//            downloader.setProxyProvider(CustomProxyProvider.from(proxy));
//            log.debug("代理IP已刷新：" + proxy);
//            return true;
//        }
        List<Proxy> proxies = proxyService.getAvailableProxyByProxyPoolAsync();
        if (proxies != null && !proxies.isEmpty()) {
            Proxy[] proxiesArray = proxies.toArray(new Proxy[0]);
            downloader.setProxyProvider(SimpleProxyProvider.from(proxiesArray));
            return true;
        }
        log.warn("暂时无法获取有效代理！");
        downloader.setProxyProvider(null);
        EnhancedHttpClientDownloader.ALLOWS_LOCAL_IP = true;
        return false;
    }

    /**
     * 获取单个有效代理
     */
    public Proxy getAvailableProxyByProxyPool() {
        String ip = proxyRedisRepository.getProxyString();
        return StringUtils.isEmpty(ip) ? null : ProxyUtil.buildProxy(ip);
    }

    /**
     * 获取n个有效代理
     */
    public List<Proxy> getAvailableProxyByProxyPoolAsync() {
        List<String> proxyStringList = proxyRedisRepository.getProxyStringsByNative(3);
        return proxyStringList.isEmpty() ? null : proxyStringList.stream().map(ProxyUtil::buildProxy).collect(Collectors.toList());
    }

    /**
     * 获取n个代理（未验证）
     */
    public List<Proxy> getAvailableProxiesByProxyPool() {
        final List<String> ips = proxyRedisRepository.getProxyStrings(10);
        return ips.isEmpty() ? null : ips.stream().map(ProxyUtil::buildProxy).collect(Collectors.toList());
    }

    //    @Autowired
    private ProxyIpRepository proxyIpRepository;

    /**
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
