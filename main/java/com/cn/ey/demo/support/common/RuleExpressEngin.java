package com.cn.ey.demo.support.common;

import cn.hutool.crypto.digest.MD5;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RuleExpressEngin {
    private static final ConcurrentHashMap<String, Template> SCRIPT_CACHE = new ConcurrentHashMap<>();

    private static final SimpleTemplateEngine TEMPLATE_ENGINE = new SimpleTemplateEngine();

    private static final String CONDITION_STR_TEMPLATE = "${%s ? true : false}";

    private static final String EXECUTE_STR_TEMPLATE = "${%s}";

    private static final String ARGUMENT_STR_NAME = "param";

    public static boolean condition(String condition, String argv) {
        Map<String, Object> context = new HashMap<>(1);
        context.put(ARGUMENT_STR_NAME, argv);

        String conditionTemplate = String.format(CONDITION_STR_TEMPLATE, condition);
        try {
            String key = MD5.create().digestHex(condition);
            SCRIPT_CACHE.putIfAbsent(key, TEMPLATE_ENGINE.createTemplate(conditionTemplate));
            return Boolean.parseBoolean(SCRIPT_CACHE.get(key).make(context).toString());
        } catch (Exception e) {
            throw new RuntimeException("模板解析异常" + conditionTemplate);
        }
    }

    public static void execute(String execute, String argv) {
        Map<String, Object> context = new HashMap<>(1);
        context.put(ARGUMENT_STR_NAME, argv);

        String executeTemplate = String.format(EXECUTE_STR_TEMPLATE, execute);
        try {
            String key = MD5.create().digestHex(execute);
            SCRIPT_CACHE.putIfAbsent(key, TEMPLATE_ENGINE.createTemplate(executeTemplate));
            SCRIPT_CACHE.get(key).make(context).writeTo(new StringWriter());
        } catch (Exception e) {
            throw new RuntimeException("模板解析异常" + executeTemplate);
        }
    }

    public static void main(String[] args) {
        String condition = "param.length() == 4 && param.equals(\"data\")";
        String execute = "System.out.println(param); int a = 1+1; System.out.println(a);";

        String data = "data";
        if (condition(condition, data)) {
            execute(execute, data);
        }
    }
}
