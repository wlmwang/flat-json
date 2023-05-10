package com.cn.ey.demo.support.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JsonPackEntity {
    String field();

    boolean disable() default false;
}
