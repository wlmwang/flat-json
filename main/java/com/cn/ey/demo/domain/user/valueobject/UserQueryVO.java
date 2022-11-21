package com.cn.ey.demo.domain.user.valueobject;

import lombok.Data;

@Data
public class UserQueryVO {
    private Long id;

    private String name;

    private Integer age;

    private String family;
}
