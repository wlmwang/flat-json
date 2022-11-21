package com.cn.ey.demo.support.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonPackField {
    String value() default "";

    boolean disable() default false;
}
