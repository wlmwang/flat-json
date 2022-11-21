package com.cn.ey.demo.domain.user.valueobject;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserQueryVO {
    private Long id;

    private String name;

    private Integer age;

    private String family;

    Map<String, String> searchRuleMap;

    List<Map<String, String>> sortRuleList;
}
