package com.cn.ey.demo.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class UserQuery {
    private Long id;

    private String name;

    private Integer age;

    private String family;

    // 当前安永，"包含"实际是like查找；在引入数组后可使用"包含like"，"包含in"进行区分
    Map<String, String> searchRuleMap = new HashMap<String, String>() {{
        put("name", "%like%");
        put("age", "=");
    }};

    List<Map<String, String>> sortRuleList = new ArrayList<Map<String, String>>() {{
        add(new HashMap<String, String>() {{
            put("age", "desc");
        }});
    }};
}
