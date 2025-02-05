package com.example.config;

import feign.Client;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Slf4j
@Configuration
public class FeignConfig {

    // CookieStore를 외부에서 확인할 수 있도록 메서드 제공
    private final BasicCookieStore cookieStore = new BasicCookieStore();

    @Bean
    public Client feignClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
        return new ApacheHttpClient(httpClient);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        requestTemplate.header("Cookie", cookie.getName() + "=" + cookie.getValue());
                    }
                }
            }
        };
    }
}
