package com.cn.ey.demo.support.converter;

import com.cn.ey.demo.support.annotation.JsonPackEntity;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class JsonPackHttpMessageConverters {
    public static final String APPLICATION_JSON_VALUE = "application/json";

    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

    private static volatile JsonPackHttpMessageConverter instance_;

    public static JsonPackHttpMessageConverter getConverter() {
        if (instance_ == null) {
            synchronized (JsonPackHttpMessageConverters.class) {
                if (instance_ == null) {
                    instance_ = new JsonPackHttpMessageConverter();
                }
            }
        }
        return instance_;
    }

    /**
     * 将对象实体序列化为JSON字符串
     * JsonPackHttpMessageConverters.serialize(JavaBen.class, javaBean);
     * JsonPackHttpMessageConverters.serialize(new TypeReference<List<JavaBean>>(){}.getType(), javaBeanList);
     *
     * @param entity 对象实体类型
     * @param object 对象实体
     * @return JSON字符串
     * @throws IOException
     */
    public static <T> String serialize(Type entity, T object) throws IOException {
        return serialize(entity, object, APPLICATION_JSON_VALUE);
    }
    public static <T> String serialize(Type entity, T object, String contentType) throws IOException {
        return getConverter().serialize(entity, object, contentType);
    }

    /**
     * 将JSON字节数组反序列化为对象实体
     * JsonPackHttpMessageConverters.deserialize(new TypeReference<List<JavaBean>>(){}.getType(), body.getBytes(StandardCharsets.UTF_8));
     * JsonPackHttpMessageConverters.deserialize(JavaBean.class, body.getBytes(StandardCharsets.UTF_8));
     *
     * @param entity 对象实体类型
     * @param json JSON字节数组
     * @return 对象实体
     * @throws IOException
     */
    public static <T> T deserialize(Type entity, byte[] json) throws IOException {
        return deserialize(entity, json, APPLICATION_FORM_URLENCODED_VALUE);
    }
    public static <T> T deserialize(Type entity, byte[] json, String contentType) throws IOException {
        return deserialize(entity, null, json, contentType);
    }
    public static <T> T deserialize(Type entity, Class<?> controller, byte[] json, String contentType) throws IOException {
        return getConverter().deserialize(entity, controller, json, contentType);
    }

    /**
     * 获取对象实体类型中注解了@JsonPackEntity的打包字段
     * @param type 可以是泛型参数中嵌套了注解了@JsonPackEntity对象实体类型的对象实体类型
     * @return
     */
    public static Field getJsonPackEntityField(Type type) {
        return getConverter().findJsonPackEntityField(type);
    }

    /**
     * 获取对象实体类型中所有未被注解了@JsonPackEntity的字段
     * @param clazz 只能是直接被注解了@JsonPackEntity的对象实体类型
     * @return
     */
    public static List<Field> getNoneJsonPackEntityField(Class<?> clazz) {
        JsonPackEntity clazzAnnotation = clazz.getAnnotation(JsonPackEntity.class);
        if (clazzAnnotation == null || clazzAnnotation.disable() || !StringUtils.hasText(clazzAnnotation.field())) {
            return null;
        }

        Field[] allFields = ReflectUtil.getFields(clazz);
        Field jsonPackEntityField = getJsonPackEntityField(clazz);
        return Arrays.stream(allFields).filter(field -> {
            if (Objects.isNull(jsonPackEntityField)) {
                return true;
            }
            return !jsonPackEntityField.getName().equals(field.getName());
        }).collect(Collectors.toList());
    }
}
