package com.cn.ey.demo.controller.dto;

import lombok.Data;

import java.util.*;

@Data
public class UserQuery {
    private Long id;

    private String name;

    private Integer age;

    private String sex;

    private String addr;

    private String family;

    // 当前安永，"包含"实际是like查找；在引入数组后可使用"包含like"，"包含in"进行区分
    Map<String, String> searchRuleMap = new HashMap<String, String>() {{
        put("name", "%like%");
        put("addr", "%like%");
        put("sex", "=");
        put("age", "=");
    }};

    List<Map<String, String>> sortRuleList = new ArrayList<Map<String, String>>() {{
        add(new LinkedHashMap<String, String>() {{
            put("age", "desc");
        }});
        add(new LinkedHashMap<String, String>() {{
            put("id", "asc");
        }});
    }};
}
