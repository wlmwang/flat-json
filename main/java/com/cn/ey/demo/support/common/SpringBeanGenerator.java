package com.cn.ey.demo.support.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class SpringBeanGenerator {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(^(\\-)?\\d{1,16}(\\.\\d{0,12}(E|e)?\\d{0,12})?)$|(0E-\\d{0,3})");

    public static Object generate(LinkedHashMap<String, Object> param) {
        org.springframework.cglib.beans.BeanGenerator generator = new org.springframework.cglib.beans.BeanGenerator();
        param.keySet().forEach((field) -> {
            Object value = param.get(field);
            if (Objects.isNull(value)) {
                value = "";
            }

            if (NUMBER_PATTERN.matcher(value.toString()).matches()) {
                generator.addProperty(field, Double.class);
                param.put(field, NumberUtils.parseNumber(value.toString(), Double.class));
            } else {
                generator.addProperty(field, value.getClass());
            }

        });
        Object o = generator.create();
        Iterator<String> var3 = param.keySet().iterator();

        while(var3.hasNext()) {
            String obj = var3.next();
            try {
                String methodStr = "set" + StringUtils.capitalize(obj);
                Object objeValue = param.get(obj);
                Method method;
                if (objeValue instanceof Date) {
                    method = o.getClass().getDeclaredMethod(methodStr, String.class);
                } else {
                    method = o.getClass().getDeclaredMethod(methodStr, param.get(obj).getClass());
                }

                if (param.get(obj) instanceof Double) {
                    method.invoke(o, param.get(obj));
                } else {
                    method.invoke(o, param.get(obj));
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException var9) {
                log.info(var9.getMessage(), var9);
            }
        }

        return o;
    }

    public static List generate(List<LinkedHashMap<String, Object>> param) {
        List list = new ArrayList();
        param.forEach((item) -> {
            list.add(generate(item));
        });
        return list;
    }

    public static LinkedHashMap<String, Object> formatHumpName(LinkedHashMap<String, Object> map) {
        LinkedHashMap<String, Object> result = new LinkedHashMap();
        map.forEach((key, value) -> {
            result.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key).toUpperCase(), value);
        });
        return result;
    }


    public static void main(String[] args) throws JsonProcessingException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", 100L);
        map.put("name", "jack");
        map.put("hobby", Arrays.asList("zh", "en"));

        Object generate = SpringBeanGenerator.generate(map);
        System.out.printf("Object:" + generate);

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.printf("Json:" + objectMapper.writeValueAsString(generate));
    }
}

