package com.cn.ey.demo.controller.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BaseResponse<U, T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Integer status;
    public U message;

    public T result;

    public static <U, T> BaseResponse<U, T> success() {
        return new BaseResponse<U, T>(null);
    }

    public static <U, T> BaseResponse<U, T> success(T result) {
        return new BaseResponse<U, T>(result);
    }

    public static <U, T> BaseResponse<U, T> success(U message, T result) {
        return new BaseResponse<U, T>(HttpStatus.OK.value(), result, message);
    }

    public static <U, T> BaseResponse<U, T> failure() {
        return new BaseResponse<U, T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);
    }

    public static <U, T> BaseResponse<U, T> failure(int code, U message) {
        return new BaseResponse<U, T>(code, null, message);
    }

    public BaseResponse(T result) {
        this(HttpStatus.OK.value(), result);
    }

    public BaseResponse(int status, T result) {
        this(status, result, null);
    }

    public BaseResponse(int status, T result, U message) {
        this.status = status;
        this.result = result;
        this.message = message;
    }
}
