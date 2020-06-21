package com.example.webmagic.pipeline;

import com.example.webmagic.dao.ProxyIpRepository;
import com.example.webmagic.entity.ProxyIp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yinfelix
 * @date 2020/6/15
 * @deprecated 改用第三方方案，不再将代理站点保存至MySQL
 */
//@Component
@Slf4j
public class ProxyIpPipeline extends SimpleListPersistencePipeline<ProxyIp> {
    private final ProxyIpRepository repository;

    public ProxyIpPipeline(ProxyIpRepository repository) {
        this.repository = repository;
        if (repository.count() != 0) {
//            repository.deleteAllInBatch();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processEach(ProxyIp proxyIp) {
        String ip = proxyIp.getIp();
        String ipPort = proxyIp.getIpPort();

        if (ip != null && ipPort != null) {
            final ProxyIp existingProxyIp = repository.findByIpAndIpPort(ip, ipPort);
            if (existingProxyIp != null) {
                proxyIp.setId(existingProxyIp.getId());
            }
            if (!proxyIp.equals(existingProxyIp)) {
                ProxyIp saveResult = repository.saveAndFlush(proxyIp);
                if (saveResult.getId() != null) {
                    log.debug(ip + ":" + ipPort + "已更新");
                } else {
                    log.error(ip + ":" + ipPort + "暂时无法保存至MySQL！");
                }
            } else {
                log.debug(ip + ":" + ipPort + "无需更新");
            }
        }
    }
}
