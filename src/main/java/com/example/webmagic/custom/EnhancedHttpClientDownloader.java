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
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
            logger.warn("页面{}下载被拒绝: {}", request.getUrl(), httpResponse.getStatusLine());
            page.setDownloadSuccess(false);
            proxyService.refreshDownloaderProxy(this);
        } else {
            logger.info("😎 页面{}下载成功～", request.getUrl());
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

    /**
     * TODO 多个爬虫线程共享此变量，暂时未实现对每一线程下载失败次数进行单独计数
     */
    private final AtomicInteger timeoutCount = new AtomicInteger(0);
//    private List<String> timeoutUrls = Collections.synchronizedList(new ArrayList<>());

    public AtomicInteger getTimeoutCount() {
        return timeoutCount;
    }

//    public List<String> getTimeoutUrls() {
//        return timeoutUrls;
//    }

    /*
    每次进行循环重试时，Spider将分配给新的线程，故ThreadLocal无效
     */
//    public static final ThreadLocal<Boolean> ALLOWS_LOCAL_IP = ThreadLocal.withInitial(() -> false);
    public static boolean ALLOWS_LOCAL_IP = false;

    @Override
    protected void onError(Request request) {
        if (proxyProvider != null) {
            timeoutCount.incrementAndGet();
//            timeoutUrls.add(request.getUrl());
            if (getTimeoutCount().compareAndSet(SpiderConstant.MAX_RETRY_TIMES, 0)) {
                logger.warn("{}超时次数已达上限！", request.getUrl());
                try {
                    Thread.sleep(SpiderConstant.BASE_SLEEP_INTERVAL);
                } catch (InterruptedException ignored) {
                }
                logger.debug("暂时不再获取代理 :(");
                /*
                若超时次数已达上限，则暂时使用本机IP进行爬取
                 */
                this.setProxyProvider(null);
                ALLOWS_LOCAL_IP = true;
            } else {
                /*
                超时次数尚未达上限时，才进行代理刷新
                 */
                proxyService.refreshDownloaderProxy(this);
            }
        } else {
            /*
            TODO 进行邮件提醒
             */
            logger.error("⚠️使用本机IP爬取失败！⚠️");
            logger.debug("尝试重新获取代理...");
            proxyService.refreshDownloaderProxy(this);
        }
    }

    @Override
    protected void onSuccess(Request request) {
        if (proxyProvider != null) {
            logger.debug("下一次下载将沿用本次代理～");
        }
        /*
        使用本机IP进行爬取后，对同一线程下一请求再次禁用本机IP
         */
        else {
            ALLOWS_LOCAL_IP = false;
            /*
            使用本机IP下载页面成功后，不再进行阻塞式代理刷新
             */
//            logger.debug("尝试重新获取代理...");
//            proxyService.refreshDownloaderProxy(this);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    @SneakyThrows
    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site cannot be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(task.getSite());

        Proxy proxy = null;
        /*
        是否在每次爬取前强制获取新代理（取决于代理时效性与爬取频度，此处暂时关闭）
         */
//        proxyService.refreshDownloaderProxy(this);
        logger.debug("本次下载是否已获取代理？{} 可否使用本机IP？{}", proxyProvider != null, ALLOWS_LOCAL_IP);
        while (proxyProvider == null) {
            if (ALLOWS_LOCAL_IP) {
                logger.warn("本次爬取暂时使用本机IP！");
                break;
            } else {
                proxyService.refreshDownloaderProxy(this);
            }
        }
        if (proxyProvider != null) {
            proxy = proxyProvider.getProxy(task);
            logger.info("当前请求IP为：" + proxy.getHost() + ":" + proxy.getPort());
        }
        TimeUnit.SECONDS.sleep(new Random().nextInt(5));

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
