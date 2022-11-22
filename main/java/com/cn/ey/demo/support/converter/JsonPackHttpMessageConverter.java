package com.cn.ey.demo.support.converter;

import com.cn.ey.demo.support.annotation.JsonPackEntity;
import com.cn.ey.demo.support.annotation.JsonPackField;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    /**
     * 一个实体类只能又一个JsonPackField字段
     */
    private final Map<Class<?>, Field> cachedJsonPackField = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> cachedNoneJsonPackField = new ConcurrentHashMap<>();

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
        return findJsonPackField(clazz) != null;
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return findJsonPackField(type) != null;
    }

    @Override
    public boolean canWrite(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return findJsonPackField(type) != null;
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        String body = StreamUtils.copyToString(inputMessage.getBody(), charset);
        HttpInputMessage inputMessageWrapper = inputMessage;

        Field jsonPackField = findJsonPackField(type);
        if (jsonPackField != null) {
            Object objectNode = null;
            if (type instanceof ParameterizedType) {
                if ("java.util.List".equalsIgnoreCase(((ParameterizedType) type).getRawType().getTypeName())) {
                    ArrayNode arrNode = defaultObjectMapper.readValue(body, ArrayNode.class);
                    ArrayNode newNode = defaultObjectMapper.createArrayNode();
                    for (Iterator<JsonNode> elements = arrNode.elements(); elements.hasNext(); ) {
                        JsonNode jsonNode = elements.next();
                        // 对象数组 - 仅支持一维的数组，直接存放键值对的JSON
                        if (jsonNode.isObject()) {
                            newNode.add(readNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(jsonNode.toString(), JsonNode.class), OPT_.PACK));
                        }
                    }
                    objectNode = newNode;
                }
            } else {
                objectNode = readNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(body, JsonNode.class), OPT_.PACK);
            }

            final Object finalObjectNode = objectNode;
            inputMessageWrapper = new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    return new ByteArrayInputStream(defaultObjectMapper.writeValueAsBytes(finalObjectNode));
                }
                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        }

        return super.read(type, contextClass, inputMessageWrapper);
    }

    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        Field jsonPackField = findJsonPackField(type);
        if (jsonPackField != null) {
            object = defaultObjectMapper.readTree(defaultObjectMapper.writeValueAsString(object));
            // FIXME 泛型嵌套
            if (type instanceof ParameterizedType) {
                if ("java.util.List".equalsIgnoreCase(((ParameterizedType) type).getRawType().getTypeName())) {
                    ArrayNode arrNode = defaultObjectMapper.readValue(object.toString(), ArrayNode.class);
                    ArrayNode newNode = defaultObjectMapper.createArrayNode();
                    for (Iterator<JsonNode> elements = arrNode.elements(); elements.hasNext(); ) {
                        JsonNode jsonNode = elements.next();
                        // 对象数组 - 仅支持一维的数组，直接存放键值对的JSON
                        if (jsonNode.isObject()) {
                            newNode.add(readNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(jsonNode.toString(), JsonNode.class), OPT_.UNPACK));
                        }
                    }
                    object = newNode;
                }
            } else {
                object = readNode(getRawType(type), (ObjectNode) defaultObjectMapper.readValue(object.toString(), JsonNode.class), OPT_.UNPACK);
            }
        }

        super.writeInternal(object, type, outputMessage);
    }

    private ObjectNode readNode(Class<?> rawClass, ObjectNode objectNode, OPT_ opt_) throws IOException {
        return parse(rawClass, objectNode, opt_);
    }
    private ObjectNode parse(Class<?> clazz, ObjectNode jsonObject, OPT_ opt_) throws IOException {
        Field jsonPackField = findJsonPackField(clazz);
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
            // 如果存储的键值没有查询场景，可以将其定义为JSON字段。多一个兼容也没坏处，尽管好像也没啥用。。。
            if ("java.lang.String".equalsIgnoreCase(jsonPackField.getType().getTypeName())) {
                // jsonObject.putIfAbsent(jsonPackField.getName(), packNode);
                jsonObject.put(jsonPackField.getName(), packNode.toString());
            } else if ("java.util.Map".equalsIgnoreCase(jsonPackField.getType().getTypeName())) {
                jsonObject.replace(jsonPackField.getName(), packNode);
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

    List<Field> findNoneJsonPackField(Type type) {
        Class<?> clazz = getRawType(type);
        if (clazz == null) {
            return null;
        }

        // FIXME 有穿透风险
        return cachedNoneJsonPackField.computeIfAbsent(clazz, cl -> {
            Field[] allFields = cl.getDeclaredFields();
            Field jsonPackField = findJsonPackField(cl);
            return Arrays.stream(allFields).filter(field -> {
                if (jsonPackField == null) {
                    return true;
                }
                return !jsonPackField.getName().equals(field.getName());
            }).collect(Collectors.toList());
        });
    }

    Field findJsonPackField(Type type) {
        Class<?> clazz = getRawType(type);
        if (clazz == null) {
            return null;
        }

        // FIXME 有穿透风险
        return cachedJsonPackField.computeIfAbsent(clazz, cl -> {
            // 类必须要@JsonPackEntity
            JsonPackEntity clazzAnnotation = cl.getAnnotation(JsonPackEntity.class);
            if (clazzAnnotation == null || clazzAnnotation.disable()) {
                return null;
            }

            // 有一个字段为@JsonPackField
            Field[] fields = cl.getDeclaredFields();
            List<Field> packField = Arrays.stream(fields).filter(field -> {
                JsonPackField fieldAnnotation = field.getAnnotation(JsonPackField.class);
                return fieldAnnotation != null && !fieldAnnotation.disable();
            }).toList();

            if (packField.size() > 1) {
                throw new RuntimeException("一个实体最多能有一个@SecurityField的字段");
            } else if (packField.size() == 1) {
                return packField.get(0);
            }
            return null;
        });
    }

    // 获取泛型中首个类型参数
    // getRawType(new TypeReference<List<JavaBean>>(){}.getType()) ---> JavaBean.class
    Class<?> getRawType(Type type) {
        if (type instanceof TypeVariable) {
            throw new IllegalArgumentException("不能有未确认的泛型参数");
        } else if (type instanceof ParameterizedType) {
            ResolvableType resolvedType = ResolvableType.forType(type);
            if (resolvedType.hasUnresolvableGenerics()) {
                throw new IllegalArgumentException("不能有未确认的泛型参数");
            }

            ResolvableType[] resolvableTypes = resolvedType.getGenerics();
            if ((resolvableTypes[0].getType() instanceof TypeVariable) ||
                    (resolvableTypes[0].getType() instanceof ParameterizedType)) {
                return getRawType(resolvableTypes[0].getType());
            } else if (resolvableTypes[0].getRawClass() != null) {
                type = resolvableTypes[0].getRawClass();
            }
        }

        return (type instanceof Class<?>) ? (Class<?>) type : null;
    }
}
