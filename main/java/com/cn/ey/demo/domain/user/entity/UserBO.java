package com.cn.ey.demo.domain.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cn.ey.demo.support.annotation.JsonPackEntity;
import lombok.Data;

import java.util.Map;

@Data
@TableName(value = "user", autoResultMap = true)
@JsonPackEntity(field = "extension")
public class UserBO {
    @TableId(value = "id")
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "dummy", exist = false)
    private String dummy;

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
