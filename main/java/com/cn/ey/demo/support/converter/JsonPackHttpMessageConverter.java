package com.cn.ey.demo.support.converter;

import com.cn.ey.demo.support.annotation.JsonPackEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonPackHttpMessageConverter extends MappingJackson2HttpMessageConverter {
    private enum OPT_ { PACK, UNPACK }

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Charset charset = DEFAULT_CHARSET;

    private final List<MediaType> supportedMediaTypes = new ArrayList<>();

    private final Map<Class<?>, Field> cachedJsonPackEntityField = new ConcurrentHashMap<>();

    public JsonPackHttpMessageConverter() {
        // objectMapper = Jackson2ObjectMapperBuilder.json().build();
        init();
    }
    public JsonPackHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
        init();
    }

    private void init() {
        this.supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        this.supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);

        /*
         * TODO Java中的Long比Javascript中的Number范围更大，这可能导致在反序列化时，部分数值在Javascript中精度丢失
         *  Java:
         *      Long.MIN_VALUE = -2^63 = -9223372036854775808
         *      Long.MAX_VALUE = 2^63 - 1 = 9223372036854775807
         *  Javascript:
         *      Number.MAX_SAFE_INTEGER = 2^53 - 1 => 9007199254740991
         *      Number.MIN_SAFE_INTEGER = -(2^53 - 1) => -9007199254740991
         */
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        defaultObjectMapper.registerModule(simpleModule);
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(this.supportedMediaTypes);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return findJsonPackEntityField(clazz) != null;
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return findJsonPackEntityField(type) != null;
    }

    @Override
    public boolean canWrite(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return findJsonPackEntityField(type) != null;
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        HttpInputMessage inputMessageWrapper = inputMessage;

        Field jsonPackField = findJsonPackEntityField(type);
        if (jsonPackField != null) {
            String body = StreamUtils.copyToString(inputMessage.getBody(), charset);
            // FIXME 泛型嵌套
            Object object;
            if (type instanceof ParameterizedType) {
                object = readNode(ResolvableType.forType(type), body);
            } else {
                object = parseNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(body, JsonNode.class), OPT_.PACK);
            }
            if (object != null) {
                inputMessageWrapper = new HttpInputMessage() {
                    @Override
                    public InputStream getBody() throws IOException {
                        return new ByteArrayInputStream(defaultObjectMapper.writeValueAsBytes(object));
                    }
                    @Override
                    public HttpHeaders getHeaders() {
                        return inputMessage.getHeaders();
                    }
                };
            }
        }

        return super.read(type, contextClass, inputMessageWrapper);
    }

    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        Field jsonPackField = findJsonPackEntityField(type);

        Object objectNode = null;
        if (jsonPackField != null) {
            // FIXME 泛型嵌套
            if (type instanceof ParameterizedType) {
                objectNode = writeNode(ResolvableType.forType(type), object);
            } else {
                objectNode = defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(object));
                objectNode = parseNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(objectNode.toString(), JsonNode.class), OPT_.UNPACK);
            }
        }

        super.writeInternal(objectNode != null? objectNode: object, type, outputMessage);
    }

    @SuppressWarnings("unchecked")
    private Object readNode(ResolvableType resolvedType, Object object) throws IOException {
        Type rawType = null;
        Field jsonPackField = null;
        for (ResolvableType resolvableType : resolvedType.getGenerics()) {
            rawType = resolvableType.getType();
            if (rawType instanceof TypeVariable) {
                if (resolvedType.getRawClass() != null) {
                    // 处理泛型嵌套，比如 - List<List<List<JavaBean>>>
                    resolvedType = ResolvableType.forClassWithGenerics(resolvedType.getRawClass(), ResolvableType.forType(resolvedType.getGeneric().resolve()));
                } else {
                    continue;
                }
            }
            if (resolvableType.hasGenerics()) {
                rawType = resolvableType.getType();
            } else {
                rawType = resolvableType.getType();
                if (rawType instanceof TypeVariable) {
                    // 处理泛型嵌套，比如 - List<List<List<JavaBean>>>
                    rawType = resolvableType.resolve();
                }
            }
            jsonPackField = findJsonPackEntityField(rawType);
            if (jsonPackField != null) {
                break;
            }
        }

        // 是否存在JSON打包字段
        if (jsonPackField == null) {
            return null;
        }

        if (resolvedType.hasGenerics()) {
            Class<?> clazz = resolvedType.getRawClass();
            if (clazz == null && resolvedType.getType() instanceof TypeVariable) {
                clazz = resolvedType.resolve();
            }
            if (clazz == null) {
                return null;
            }

            if (List.class.isAssignableFrom(clazz)) {
                ArrayNode arrNode;
                if (!(object instanceof String)) {
                    arrNode = (ArrayNode) defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(object));
                } else {
                    arrNode = defaultObjectMapper.readValue(object.toString(), ArrayNode.class);
                }
                ArrayNode newNode = defaultObjectMapper.createArrayNode();
                for (Iterator<JsonNode> elements = arrNode.elements(); elements.hasNext(); ) {
                    JsonNode jsonNode = elements.next();
                    if (jsonNode.isObject()) {
                        newNode.add(parseNode(getRawType(resolvedType.getType()), (ObjectNode) defaultObjectMapper.readValue(jsonNode.toString(), JsonNode.class), OPT_.PACK));
                    } else {
                        newNode.add((BaseJsonNode) readNode(resolvedType.getGeneric(), jsonNode));
                    }
                }
                object = newNode;
            } else {
                Map<String, Object> objectMap;
                if (!(object instanceof Map<?,?>)) {
                    objectMap = defaultObjectMapper.readValue(object.toString(), new TypeReference<Map<String, Object>>() {});
                } else {
                    objectMap = (Map<String, Object>) object;
                }

                Field[] fields = clazz.getDeclaredFields();
                for (Field field: fields) {
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType || fieldType instanceof TypeVariable) {
                        Object fieldValue = SystemMetaObject.forObject(objectMap).getValue(field.getName());
                        BaseJsonNode jsonNode = (BaseJsonNode) readNode(ResolvableType.forField(field, resolvedType), fieldValue);
                        // 在当前字段中，递归泛型参数后，找到了打包字段
                        if (jsonNode != null) {
                            SystemMetaObject.forObject(objectMap).setValue(field.getName(), null);
                            JsonNode leftObject = defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(objectMap));
                            ObjectNode leftNode = defaultObjectMapper.readValue(leftObject.toString(), ObjectNode.class);
                            leftNode.replace(field.getName(), jsonNode);
                            return leftNode;
                        }
                    }
                }
            }
        } else {
            object = parseNode(getRawType(resolvedType.getType()), (ObjectNode) defaultObjectMapper.readValue(object.toString(), JsonNode.class), OPT_.PACK);
        }

        return object;
    }

    private Object writeNode(ResolvableType resolvedType, Object object) throws IOException {
        Type rawType = null;
        Field jsonPackField = null;
        for (ResolvableType resolvableType : resolvedType.getGenerics()) {
            rawType = resolvableType.getType();
            if (rawType instanceof TypeVariable) {
                if (resolvedType.getRawClass() != null) {
                    // 处理泛型嵌套，比如 - List<List<List<JavaBean>>>
                    resolvedType = ResolvableType.forClassWithGenerics(resolvedType.getRawClass(), ResolvableType.forType(resolvedType.getGeneric().resolve()));
                } else {
                    continue;
                }
            }
            if (resolvableType.hasGenerics()) {
                rawType = resolvableType.getType();
            } else {
                rawType = resolvableType.getType();
                if (rawType instanceof TypeVariable) {
                    // 处理泛型嵌套，比如 - List<List<List<JavaBean>>>
                    rawType = resolvableType.resolve();
                }
            }
            jsonPackField = findJsonPackEntityField(rawType);
            if (jsonPackField != null) {
                break;
            }
        }

        // 是否存在JSON打包字段
        if (jsonPackField == null) {
            return null;
        }

        if (resolvedType.hasGenerics()) {
            if (object instanceof BaseJsonNode) {
                object = defaultObjectMapper.readValue(object.toString(), resolvedType.getRawClass());
            }
            Class<?> clazz = ResolvableType.forInstance(object).getRawClass();
            if (clazz == null) {
                return null;
            }
            if (List.class.isAssignableFrom(clazz)) {
                object = defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(object));
                ArrayNode arrNode = defaultObjectMapper.readValue(object.toString(), ArrayNode.class);
                ArrayNode newNode = defaultObjectMapper.createArrayNode();
                for (Iterator<JsonNode> elements = arrNode.elements(); elements.hasNext(); ) {
                    JsonNode jsonNode = elements.next();
                    if (jsonNode.isObject()) {
                        newNode.add(parseNode(getRawType(rawType), (ObjectNode) defaultObjectMapper.readValue(jsonNode.toString(), JsonNode.class), OPT_.UNPACK));
                    } else {
                        newNode.add((BaseJsonNode) writeNode(ResolvableType.forType(rawType), jsonNode));
                    }
                }
                return newNode;
            } else {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field: fields) {
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType || fieldType instanceof TypeVariable) {
                        Object fieldValue = SystemMetaObject.forObject(object).getValue(field.getName());
                        BaseJsonNode jsonNode = (BaseJsonNode) writeNode(ResolvableType.forField(field, resolvedType), fieldValue);
                        // 在当前字段中，递归泛型参数后，找到了打包字段
                        if (jsonNode != null) {
                            SystemMetaObject.forObject(object).setValue(field.getName(), null);
                            JsonNode leftObject = defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(object));
                            ObjectNode leftNode = defaultObjectMapper.readValue(leftObject.toString(), ObjectNode.class);
                            leftNode.replace(field.getName(), jsonNode);
                            return leftNode;
                        }
                    }
                }
            }
        } else {
            object = parseNode(getRawType(rawType), (ObjectNode) defaultObjectMapper.readValue(object.toString(), JsonNode.class), OPT_.UNPACK);
        }

        return object;
    }

    private ObjectNode parseNode(Class<?> clazz, ObjectNode jsonObject, OPT_ opt_) throws IOException {
        Field jsonPackField = findJsonPackEntityField(clazz);
        if (jsonPackField == null) {
            return jsonObject;
        }

        // 普通字段（没有 @JsonPackField 注解的字段）
        Map<String, Field> normalFiledNames = Arrays.stream(clazz.getDeclaredFields()).filter(
                field ->  !jsonPackField.getName().equals(field.getName())
        ).collect(Collectors.toMap(Field::getName, v -> v));

        if (opt_ == OPT_.PACK) {
            // 将请求数据，打包到方法接收参数为@JsonPackField字段中
            ObjectNode packNode = defaultObjectMapper.createObjectNode();
            for (Iterator<String> it = jsonObject.fieldNames(); it.hasNext(); ) {
                String filedName = it.next();
                if (!normalFiledNames.containsKey(filedName)) {
                    JsonNode fieldNode = jsonObject.get(filedName);
                    if (fieldNode.isArray()) {
                        ArrayNode arrNode = defaultObjectMapper.createArrayNode();
                        for (Iterator<JsonNode> elements = fieldNode.elements(); elements.hasNext();) {
                            JsonNode jsonNode = elements.next();
                            if (jsonNode.isValueNode()) {
                                arrNode.add(jsonNode);
                            }
                        }
                        packNode.putIfAbsent(filedName, arrNode);
                    } else if (fieldNode.isValueNode()) {
                        packNode.putIfAbsent(filedName, fieldNode);
                    }
                    it.remove();
                }
            }

            // TODO 打包字段如果是字符串类型，入库时会自动添加转义字符，这会导致不能使用JSON查询语句
            if (Map.class.isAssignableFrom(jsonPackField.getType())) {
                jsonObject.replace(jsonPackField.getName(), packNode);
            } else if (String.class.isAssignableFrom(jsonPackField.getType())) {
                jsonObject.put(jsonPackField.getName(), packNode.toString());
            }
        } else {
            // 将方法返回数据为@JsonPackField字段，拆解、平铺到一级字段中（二级变一级）
            JsonNode jsonNode = jsonObject.get(jsonPackField.getName());
            jsonObject.remove(jsonPackField.getName());
            // 打包字段类型为java.lang.String
            if (jsonNode.isTextual()) {
                jsonNode = new ObjectMapper().readValue(jsonNode.asText(), JsonNode.class);
            }
            for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                String filedName = it.next();
                jsonObject.putIfAbsent(filedName, jsonNode.get(filedName));
            }
        }

        return jsonObject;
    }

    Field findJsonPackEntityField(Type type) {
        if (type instanceof TypeVariable) {
            throw new RuntimeException("不能有未确认的泛型变量");
        } else if (type instanceof ParameterizedType || type instanceof Class<?>) {
            ResolvableType resolvedType = ResolvableType.forType(type);
            if (resolvedType.hasUnresolvableGenerics()) {
                throw new RuntimeException("不能有未解析的泛型参数");
            }

            List<ResolvableType> resolvableTypes;
            if (type instanceof ParameterizedType) {
                resolvableTypes = Arrays.asList(resolvedType.getGenerics());
            } else {
                resolvableTypes = Collections.singletonList(ResolvableType.forType(type));
            }
            for (ResolvableType resolvableType : resolvableTypes) {
                Class<?> clazz = null;
                if ((resolvableType instanceof TypeVariable) ||
                        (resolvableType.getType() instanceof ParameterizedType)) {
                    clazz = getRawType(resolvableType.getType());
                } else if (resolvableType.getRawClass() != null) {
                    clazz = resolvableType.getRawClass();
                }

                // FIXME Cache有穿透风险
                Field packField = cachedJsonPackEntityField.computeIfAbsent(clazz, cl -> {
                    JsonPackEntity clazzAnnotation = cl.getAnnotation(JsonPackEntity.class);
                    if (clazzAnnotation == null || clazzAnnotation.disable()) {
                        return null;
                    }

                    return Arrays.stream(cl.getDeclaredFields()).filter(
                            field -> field.getName().equals(clazzAnnotation.field())
                    ).findFirst().orElse(null);
                });
                if (packField != null) {
                    if (!(Map.class.isAssignableFrom(packField.getType()) || String.class.isAssignableFrom(packField.getType()))) {
                        throw new RuntimeException("打包字段必须是Map或者String数据类型。强烈建议使用Map类型");
                    }
                    return packField;
                }
            }
        }

        return null;
    }

    // getRawType(new TypeReference<List<JavaBean>>(){}.getType()) ---> JavaBean.class
    Class<?> getRawType(Type type) {
        if (type instanceof TypeVariable) {
            throw new RuntimeException("不能有未确认的泛型变量");
        } else if (type instanceof ParameterizedType) {
            ResolvableType resolvedType = ResolvableType.forType(type);
            if (resolvedType.hasUnresolvableGenerics()) {
                throw new RuntimeException("不能有未解析的泛型参数");
            }

            ResolvableType resolvableType = resolvedType.getGeneric();
            if ((resolvableType instanceof TypeVariable) ||
                    (resolvableType.getType() instanceof ParameterizedType)) {
                return getRawType(resolvableType.getType());
            } else if (resolvableType.getRawClass() != null) {
                type = resolvableType.getRawClass();
            }
        }

        return (type instanceof Class<?>) ? (Class<?>) type : null;
    }
}
