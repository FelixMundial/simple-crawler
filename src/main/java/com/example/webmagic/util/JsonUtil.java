package com.example.webmagic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class JsonUtil implements Ordered {
    @Autowired
    private ObjectMapper objectMapper;

    private static JsonUtil jsonUtil;

    /**
     * 工具类静态成员注入容器对象
     * （需额外在其他服务中配置工具包扫描）
     */
    @PostConstruct
    public void init() {
        jsonUtil = this;
    }

    public static <T> T collect(String string, Class<T> clazz) {
        T dto = null;
        try {
            dto = jsonUtil.objectMapper.readValue(string, clazz);
        } catch (JsonProcessingException e) {
            log.error("在将字符串解析成实体类时发生了JSON解析错误", e);
        }
        return dto;
    }

    public static <T> T collect(InputStream stream, Class<T> clazz) {
        T dto = null;
        try {
            dto = jsonUtil.objectMapper.readValue(stream, clazz);
        } catch (IOException e) {
            log.error("在将字符串解析成实体类时发生了JSON解析错误", e);
        }
        return dto;
    }

    public static String parse(Object dto) {
        String resultJsonString = null;
        try {
            resultJsonString = jsonUtil.objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("在将实体类解析成字符串实体类时发生了JSON解析错误", e);
        }
        return resultJsonString;
    }

    @Override
    public int getOrder() {
        return -10;
    }
}

