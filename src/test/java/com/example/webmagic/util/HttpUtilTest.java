package com.example.webmagic.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtilTest {
    @Test
    public void testApacheHttpClient() {

    }

    @Test
    public void testCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "f1";
        });

        f1.whenCompleteAsync((s, throwable) -> {
            System.out.println(System.currentTimeMillis() + ":" + s);
        });

        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "f2";
        });

        f2.whenCompleteAsync((s, throwable) -> {
            System.out.println(System.currentTimeMillis() + ":" + s);
        });

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(f1, f2);
        System.out.println(anyOf.get());
    }
}
