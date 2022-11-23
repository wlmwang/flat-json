package com.cn.ey.demo.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class BaseResponse<T, U> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Integer status;
    public U message;

    public T result;

    public static <T, U> BaseResponse<T, U> success() {
        return new BaseResponse<T, U>((T)null);
    }

    public static <T, U> BaseResponse<T, U> success(T result) {
        return new BaseResponse<T, U>(result);
    }

    public static <T, U> BaseResponse<T, U> success(T result, U message) {
        return new BaseResponse<T, U>(HttpStatus.OK.value(), result, message);
    }

    public static <T, U> BaseResponse<T, U> failure() {
        return new BaseResponse<T, U>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null);
    }

    public static <T, U> BaseResponse<T, U> failure(int code, U message) {
        return new BaseResponse<T, U>(code, null, message);
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
