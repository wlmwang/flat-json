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
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        // ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
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

    private List<Field> findNoneJsonPackField(Type type) {
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

    private Field findJsonPackField(Type type) {
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

    private Class<?> getRawType(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types != null && types[0] instanceof Class<?>) {
                type = types[0];
            }
        }
        if (!(type instanceof Class<?> clazz)) {
            return null;
        }
        return clazz;
    }


    // -------------------------------------------------------------------------
    // ---------------------------TODO 外部直接调用接口----------------------------
    // -------------------------------------------------------------------------

    public static Field getJsonPackField(Type type) {
        return getInstance().findJsonPackField(type);
    }
    public static List<Field> getNoneJsonPackField(Type type) {
        return getInstance().findNoneJsonPackField(type);
    }

    // TODO JsonPackHttpMessageConverter.serialize(new TypeReference<List<JavaBen>>(){}.getType(), javaBenList);
    public static <T> String serialize(Type entity, T object) throws IOException {
        return serialize(entity, object, MediaType.APPLICATION_JSON_VALUE);
    }
    public static <T> String serialize(Type entity, T object, String contentType) throws IOException {
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
        new JsonPackHttpMessageConverter().writeInternal(object, entity, outputMessage);

        return outputMessage.getBody().toString();
    }

    // TODO JsonPackHttpMessageConverter.deserialize(new TypeReference<List<JavaBen>>(){}.getType(), XXXController.class, body.getBytes(StandardCharsets.UTF_8));
    public static <T> T deserialize(Type entity, Class<?> controller, byte[] json) throws IOException {
        return deserialize(entity, controller, json, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Type entity, Class<?> controller, byte[] json, String contentType) throws IOException {
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

        return (T) getInstance().read(entity, controller, inputMessage);
    }

    private static volatile JsonPackHttpMessageConverter instance_;
    public static JsonPackHttpMessageConverter getInstance() {
        if (instance_ == null) {
            synchronized (JsonPackHttpMessageConverter.class) {
                if (instance_ == null) {
                    instance_ = new JsonPackHttpMessageConverter();
                }
            }
        }

        return instance_;
    }
}
