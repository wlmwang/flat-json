package com.cn.ey.demo.controller.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Integer status;
    public T result;
    public String message;

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<T>(null);
    }

    public static <T> BaseResponse<T> success(T result) {
        return new BaseResponse<T>(result);
    }

    public static <T> BaseResponse<T> success(T result, String message) {
        return new BaseResponse<T>(HttpStatus.OK.value(), result, message);
    }

    public static <T> BaseResponse<T> failure() {
        return new BaseResponse<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);
    }

    public static <T> BaseResponse<T> failure(int code, String message) {
        return new BaseResponse<T>(code, null, message);
    }

    public BaseResponse(T result) {
        this(HttpStatus.OK.value(), result);
    }

    public BaseResponse(int status, T result) {
        this(status, result, null);
    }

    public BaseResponse(int status, T result, String message) {
        this.status = status;
        this.result = result;
        this.message = message;
    }
}
