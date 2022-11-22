package com.cn.ey.demo.support.converter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public final class JsonPackHttpMessageConverters {
    private static volatile JsonPackHttpMessageConverter instance_;

    public static JsonPackHttpMessageConverter getConverter() {
        if (instance_ == null) {
            synchronized (JsonPackHttpMessageConverter.class) {
                if (instance_ == null) {
                    instance_ = new JsonPackHttpMessageConverter();
                }
            }
        }
        return instance_;
    }

    /**
     * 获取对象实体类型中注解了@JsonPackField的字段
     *
     * @param type 对象实体类型
     * @return
     */
    public static Field getJsonPackField(Type type) {
        return getConverter().findJsonPackField(type);
    }

    /**
     * 获取对象实体类型中所有未被注解了@JsonPackField的字段
     *
     * @param type 对象实体类型
     * @return
     */
    public static List<Field> getNoneJsonPackField(Type type) {
        return getConverter().findNoneJsonPackField(type);
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
    public static <T> String serialize(Type entity, T object)
            throws IOException {
        return serialize(entity, object, MediaType.APPLICATION_JSON_VALUE);
    }
    public static <T> String serialize(Type entity, T object, String contentType)
            throws IOException {
        HttpOutputMessage outputMessage =  new HttpOutputMessage() {
            private final OutputStream out = new ByteArrayOutputStream(1024);

            @Override
            public OutputStream getBody() { return out; }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
                return httpHeaders;
            }
        };
        getConverter().writeInternal(object, entity, outputMessage);

        return outputMessage.getBody().toString();
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
    public static <T> T deserialize(Type entity, byte[] json)
            throws IOException {
        return deserialize(entity, json, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }
    public static <T> T deserialize(Type entity, byte[] json, String contentType)
            throws IOException {
        return deserialize(entity, null, json, contentType);
    }
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Type entity, Class<?> controller, byte[] json, String contentType)
            throws IOException {
        HttpInputMessage inputMessage =  new HttpInputMessage() {
            @Override
            public InputStream getBody() { return new ByteArrayInputStream(json); }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
                return httpHeaders;
            }
        };

        return (T) getConverter().read(entity, controller, inputMessage);
    }
}
