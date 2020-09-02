package com.example.webmagic.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
     *
     * @param url
     * @param ip
     * @param port
     * @return
     */
    public static int testHttpGet(String url, String ip, String port) {
        /*final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
//                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.of(new InetSocketAddress(ip, Integer.parseInt(port))))
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING)
                .setHeader("Accept", HEADER_ACCEPT)
                .setHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE)
                .setHeader("User-Agent", UserAgentFactory.getUserAgent())
                .timeout(Duration.ofSeconds(5))
                .build();
        return testHttp(httpClient, httpRequest);*/

        int statusCode = 0;
        try {
            statusCode = testHttpGetWithApacheHttpClient(url, ip, port);
        } catch (IOException e) {
            log.error("", e);
        }
        return statusCode;
    }

    /**
     * 不使用代理IP获取HTTP响应
     *
     * @param url
     * @return
     */
    public static HttpResponse<String> testHttpGet0(String url) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
//                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("User-Agent", UserAgentFactory.getUserAgent())
                .timeout(Duration.ofSeconds(5))
                .build();
        return testHttp0(httpClient, httpRequest);
    }

    private static int testHttpGetWithApacheHttpClient(String url, String ip, String port) throws IOException {
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

            log.trace("测试中...");
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    log.debug("({}) 连接失败 -> {}", httpGet.getURI().getHost(), statusCode);
                }
                log.debug("({}) 连接成功 -> {}", httpGet.getURI().getHost(), statusCode);
                return statusCode;
            } catch (Exception e) {
                log.debug("({}) 连接失败 -> {}", httpGet.getURI().getHost(), e.getMessage());
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
    }

    private static int testHttp(HttpClient httpClient, HttpRequest httpRequest) {
        int statusCode = org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE.value();
        try {
            log.debug("测试中...");
            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            statusCode = httpResponse.statusCode();
            /*
            不需要使用异步操作
             */
//            final CompletableFuture<HttpResponse<String>> httpResponseCompletableFuture = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
//            statusCode = httpResponseCompletableFuture.thenApply(HttpResponse::statusCode).get();
            log.debug("({}) 连接成功 -> {}", httpRequest.uri().getHost(), statusCode);
        } catch (InterruptedException | /*ExecutionException | */ IOException e) {
            log.debug("({}) 连接失败 -> {}", httpRequest.uri().getHost(), e.getMessage());
        }
        return statusCode;
    }

    private static HttpResponse<String> testHttp0(HttpClient httpClient, HttpRequest httpRequest) {
        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("", e);
            return null;
        }
    }
}
