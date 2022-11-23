package com.cn.ey.demo.controller.dto;

import com.cn.ey.demo.support.annotation.JsonPackEntity;
import lombok.Data;

import java.util.Map;

@Data
@JsonPackEntity(field = "extension")
public class UserDto {
    private Long id;

    private String name;

    private Map<String, Object> extension;
}
