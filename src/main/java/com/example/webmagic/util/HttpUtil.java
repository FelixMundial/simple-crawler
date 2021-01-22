package com.example.webmagic.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yinfelix
 * @date 2020/6/17
 */
@Slf4j
public class HttpUtil {
    public static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3";
    public static final String HEADER_ACCEPT_ENCODING = "gzip, deflate";
    public static final String HEADER_ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7,el;q=0.6,es;q=0.5)";

    /**
     * 使用代理IP获取HTTP响应
     */
    public static int testHttpGetWithProxy(String url, String ip, String port) {
        int statusCode = 0;
        try {
            statusCode = doHttpGetByApacheHttpClient(url, ip, port);
        } catch (IOException e) {
            log.error("", e);
        }
        return statusCode;
    }

    /**
     * 使用本地IP获取HTTP响应
     */
    public static int testHttpGetLocally(String url) {
        HttpResponse<String> response = testHttpGetLocallyWithBody(url);
        if (response != null) {
            return response.statusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    /**
     * 使用本地IP获取HTTP完整响应体
     */
    public static HttpResponse<String> testHttpGetLocallyWithBody(String url) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("User-Agent", UserAgentFactory.getUserAgent())
                .timeout(Duration.ofSeconds(5))
                .build();
        return doHttpGetByNative(httpClient, httpRequest);
    }

    /**
     * 使用 {@link CloseableHttpClient} 获取HTTP响应（暂时用于代理IP的同步测试）
     */
    protected static int doHttpGetByApacheHttpClient(String url, String ip, String port) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHost proxy = new HttpHost(ip, Integer.parseInt(port));
            RequestConfig requestConfig = RequestConfig.custom()
                    .setProxy(proxy)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .setConnectionRequestTimeout(5000)
                    .setConnectTimeout(5000)
                    .setSocketTimeout(10000)
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);

            httpGet.setHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING);
            httpGet.setHeader("Accept", HEADER_ACCEPT);
            httpGet.setHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE);
            httpGet.setHeader("User-Agent", UserAgentFactory.getUserAgent());

            log.trace("doHttpGetByApacheHttpClient()测试中...");
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != org.apache.http.HttpStatus.SC_OK) {
                    log.trace("({}) 连接失败 -> {}", httpGet.getURI().getHost(), statusCode);
                } else {
                    log.debug("({}) 连接成功 -> {}", httpGet.getURI().getHost(), statusCode);
                }
                return statusCode;
            } catch (Exception e) {
                log.trace("({}) 连接失败 -> {}", httpGet.getURI().getHost(), e.getMessage());
                return org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
    }

    /**
     * 使用 {@link HttpClient} 获取HTTP响应（暂时用于代理IP的同步测试）
     */
    protected static HttpResponse<String> doHttpGetByNative(String url, String ip, String port) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
//                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.of(new InetSocketAddress(ip, Integer.parseInt(port))))
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING)
                .setHeader("Accept", HEADER_ACCEPT)
                .setHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE)
                .setHeader("User-Agent", UserAgentFactory.getUserAgent())
                .timeout(Duration.ofSeconds(5))
                .build();

        return doHttpGetByNative(httpClient, httpRequest);
    }

    private static HttpResponse<String> doHttpGetByNative(HttpClient httpClient, HttpRequest httpRequest) {
        log.debug("doHttpGetByNative()测试中...");
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.debug("({}) 连接成功 -> {}", httpRequest.uri().getHost(), response.statusCode());
            return response;
        } catch (IOException | InterruptedException e) {
            log.trace("({}) 连接失败 -> {}", httpRequest.uri().getHost(), e.getMessage());
            return null;
        }
    }

    /**
     * 使用 {@link HttpClient} 进行代理IP的异步测试
     */
    public static List<String> fetchValidatedIpsAsync(String url, List<String> proxyIps) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))/*.GET()*/
                .setHeader("User-Agent", UserAgentFactory.getUserAgent())
                .timeout(Duration.ofSeconds(5))
                .build();

        List<String> proxyList = new ArrayList<>();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 50, 50L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
        log.debug("fetchValidatedIpsAsync()测试中...");

        CompletableFuture[] futures = proxyIps.stream()
                .filter(proxyIp -> proxyIp.contains(":"))
                .map(proxyIp -> {
                    String[] strings = proxyIp.split(":");
                    HttpClient httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(5))
                            .proxy(ProxySelector.of(new InetSocketAddress(strings[0], Integer.parseInt(strings[1]))))
                            .executor(pool).build();
                    CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
                    return responseFuture
//                    .whenCompleteAsync((response, error) -> {
//                    if (response != null) {
//                        log.debug("({}) 连接成功 -> {}", proxyIp, response.statusCode());
//                    }
//                    if (error != null) {
//                        log.debug("({}) 连接失败 -> {}", proxyIp, error.getLocalizedMessage());
//                    }
//                })
                            .thenApply(HttpResponse::statusCode)
                            .exceptionally(error -> {
                                log.debug("({}) 连接失败 -> {}", proxyIp, error.getLocalizedMessage());
                                return HttpStatus.REQUEST_TIMEOUT.value();
                            })
                            .thenAccept(statusCode -> {
                                if (statusCode == HttpStatus.OK.value()) {
                                    log.debug("({}) 连接成功", proxyIp);
                                    proxyList.add(proxyIp);
                                } else {
                                    log.debug("({}) 连接失败 -> {}", proxyIp, statusCode);
                                }
                            });
                }).toArray(CompletableFuture[]::new);
        /*
        TODO 若使用anyOf则暂时无法排除最先返回的异常任务
         */
        CompletableFuture.allOf(futures).join();
        log.debug("fetchValidatedIpsAsync()：{}", proxyList);
        return proxyList;
    }

    protected static List<String> fetchValidatedIpsAsync(String url, String... proxyIps) {
        return fetchValidatedIpsAsync(url, Arrays.asList(proxyIps));
    }
}
