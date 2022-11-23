package com.cn.ey.demo.controller.dto;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

public class BaseResponseTest<U, T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Integer status;
    public U message;

    public T result;

    public static <U, T> BaseResponseTest<U, T> success() {
        return new BaseResponseTest<U, T>(null);
    }

    public static <U, T> BaseResponseTest<U, T> success(T result) {
        return new BaseResponseTest<U, T>(result);
    }

    public static <U, T> BaseResponseTest<U, T> success(T result, U message) {
        return new BaseResponseTest<U, T>(HttpStatus.OK.value(), result, message);
    }

    public static <U, T> BaseResponseTest<U, T> failure() {
        return new BaseResponseTest<U, T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);
    }

    public static <U, T> BaseResponseTest<U, T> failure(int code, U message) {
        return new BaseResponseTest<U, T>(code, null, message);
    }

    public BaseResponseTest(T result) {
        this(HttpStatus.OK.value(), result);
    }

    public BaseResponseTest(int status, T result) {
        this(status, result, null);
    }

    public BaseResponseTest(int status, T result, U message) {
        this.status = status;
        this.result = result;
        this.message = message;
    }
}
