package com.cn.ey.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cn.ey.demo.annotation.JsonPackEntity;
import com.cn.ey.demo.annotation.JsonPackField;
import lombok.Data;

import java.util.Map;

@Data
@TableName(value = "user", autoResultMap = true)
@JsonPackEntity
public class User {
    @TableId(value = "id")
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "dummy", exist = false)
    private String dummy;

    @JsonPackField
    @TableField(value = "extension" , typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extension;

    /*
    @TableField(value = "extension ->> '$.age'",
       insertStrategy = FieldStrategy.NEVER,
       updateStrategy = FieldStrategy.NEVER,
       select = false)
    private String age;
    */
}
