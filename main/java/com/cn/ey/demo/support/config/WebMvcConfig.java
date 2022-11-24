package com.cn.ey.demo.support.config;

import com.cn.ey.demo.support.converter.JsonPackHttpMessageConverters;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, JsonPackHttpMessageConverters.getConverter());
    }
}
