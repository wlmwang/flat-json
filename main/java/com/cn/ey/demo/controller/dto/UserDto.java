package com.cn.ey.demo.controller.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.ey.demo.support.annotation.JsonPackEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonPackEntity(field = "extension")
public class UserDto {
    private Long id;

    private String name;

    private Map<String, Object> extension;

    private BaseResponse<String, List<List<Page<List<UserDto>>>>> children;
}
