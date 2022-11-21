package com.cn.ey.demo.support.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonPackEntity {
    String value() default "";

    boolean disable() default false;
}
