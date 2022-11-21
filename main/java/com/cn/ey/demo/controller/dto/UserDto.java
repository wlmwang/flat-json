package com.cn.ey.demo.controller.dto;

import com.cn.ey.demo.support.annotation.JsonPackEntity;
import com.cn.ey.demo.support.annotation.JsonPackField;
import lombok.Data;

import java.util.Map;

@Data
@JsonPackEntity
public class UserDto {
    private Long id;

    private String name;

    @JsonPackField
    private Map<String, Object> extension;
}
