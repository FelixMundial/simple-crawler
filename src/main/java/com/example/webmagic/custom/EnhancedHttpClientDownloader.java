package com.example.webmagic.custom;

import com.example.webmagic.constant.SpiderConstant;
import com.example.webmagic.service.ProxyService;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.downloader.HttpClientRequestContext;
import us.codecraft.webmagic.downloader.HttpUriRequestConverter;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;
import us.codecraft.webmagic.utils.HttpClientUtils;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinfelix
 * @date 2020/6/18
 */
@Component
public class EnhancedHttpClientDownloader extends AbstractDownloader {
    @Autowired
    private ProxyService proxyService;

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        Page page = new Page();
        /*
        若返回状态码非200，设置为爬取失败
         */
        if (httpResponse.getStatusLine().getStatusCode() != HttpConstant.StatusCode.CODE_200) {
            page.setDownloadSuccess(false);
            logger.warn("页面{}下载被拒绝: {}", request.getUrl(), httpResponse.getStatusLine());
            proxyService.refreshDownloaderProxy(this);
        } else {
            logger.info("🎉 页面{}下载成功～", request.getUrl());
            byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
            page.setBytes(bytes);
            if (!request.isBinaryContent()) {
                if (charset == null) {
                    charset = getHtmlCharset(contentType, bytes);
                }
                page.setCharset(charset);
                page.setRawText(new String(bytes, charset));
            }
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            page.setDownloadSuccess(true);
            if (responseHeader) {
                page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
            }
        }
        return page;
    }

    private final AtomicInteger timeoutCount = new AtomicInteger(0);
//    private List<String> timeoutUrls = Collections.synchronizedList(new ArrayList<>());

    public AtomicInteger getTimeoutCount() {
        return timeoutCount;
    }

//    public List<String> getTimeoutUrls() {
//        return timeoutUrls;
//    }

    /**
     * 统计超时次数和超时Url
     */
    @SneakyThrows
    @Override
    protected void onError(Request request) {
        if (proxyProvider != null) {
            timeoutCount.incrementAndGet();
//            timeoutUrls.add(request.getUrl());
            proxyService.refreshDownloaderProxy(this);
            /*
            todo: 并发问题
             */
            if (getTimeoutCount().compareAndSet(SpiderConstant.MAX_RETRY_TIMES, 0)) {
                logger.warn("{}超时次数已达上限！", request.getUrl());
                /*
                todo: 若超时次数已达上限，则暂停爬取，或将本页面加入队列
                 */
                Thread.sleep(SpiderConstant.BASE_SLEEP_INTERVAL);
                logger.warn("暂时无法获取代理...");
                this.setProxyProvider(null);
            }
        } else {
            /*
            todo: 进行邮件提醒
             */
            logger.error("使用本机IP爬取失败");
            logger.debug("尝试重新获取代理...");
            proxyService.refreshDownloaderProxy(this);
        }
    }

    @Override
    protected void onSuccess(Request request) {
        if (proxyProvider != null) {
            logger.debug("下一次下载将沿用本次代理～");
        } else {
            logger.debug("尝试重新获取代理...");
            proxyService.refreshDownloaderProxy(this);
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

    private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    private HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();

    private ProxyProvider proxyProvider;

    private boolean responseHeader = true;

    public void setHttpUriRequestConverter(HttpUriRequestConverter httpUriRequestConverter) {
        this.httpUriRequestConverter = httpUriRequestConverter;
    }

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    private CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site);
                    httpClients.put(domain, httpClient);
                }
            }
        }
        return httpClient;
    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site cannot be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(task.getSite());

        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task) : null;
        if (proxy != null) {
            logger.info("当前请求IP为：" + proxy.getHost() + ":" + proxy.getPort());
        } else {
            logger.warn("⚠️！！本次爬取暂时使用本机IP！！ ⚠️");
        }
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);
        Page page = Page.fail();

        try {
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
            page = handleResponse(request, request.getCharset() != null ? request.getCharset() : task.getSite().getCharset(), httpResponse, task);
            onSuccess(request);
//            logger.info("页面{}下载完成", request.getUrl());
            return page;
        } catch (IOException e) {
            logger.debug("页面{}下载超时: {}", request.getUrl(), e.getMessage());
            onError(request);
            return page;
        } finally {
            if (httpResponse != null) {
                //ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
        }
    }

    @Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }
}
