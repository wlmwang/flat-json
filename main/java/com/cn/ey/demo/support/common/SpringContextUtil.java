package com.cn.ey.demo.support.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil {
    private static ApplicationContext applicationContext;

    @Autowired
    public SpringContextUtil(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext applicationContext() {
        return applicationContext;
    }

    public static void autowireBean(Object bean) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
    }
}
