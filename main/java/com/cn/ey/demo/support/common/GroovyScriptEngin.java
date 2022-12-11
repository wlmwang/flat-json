package com.cn.ey.demo.support.common;

import cn.hutool.crypto.digest.MD5;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class GroovyScriptEngin {
    private static final ConcurrentHashMap<String, GroovyObject> SCRIPT_CACHE = new ConcurrentHashMap<>();

    private static final GroovyClassLoader CLASS_LOADER = new GroovyClassLoader(GroovyScriptEngin.class.getClassLoader());

    public static Object callGroovyScript(String script, String method, Object param) {
        return callGroovyScript(script, method, param, false);
    }
    public static Object callGroovyScript(String script, String method, Object param, Boolean autowire) {
        try {
            String key = MD5.create().digestHex(script);
            Supplier<GroovyObject> supplier = () -> {
                GroovyObject object;
                try {
                    object = ((Class<GroovyObject>) CLASS_LOADER.parseClass(script)).getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException("解析脚本失败", e);
                }
                if (Boolean.TRUE.equals(autowire)) {
                    ApplicationContextUtil.applicationContext().getAutowireCapableBeanFactory().autowireBean(object);
                }
                return object;
            };
            SCRIPT_CACHE.putIfAbsent(key, supplier.get());
            return SCRIPT_CACHE.get(key).invokeMethod(method, param);
        } catch (Exception e) {
            throw new RuntimeException("脚本执行失败", e);
        }
    }

    public static void main(String[] args) {
        String groovyScript = "public class test { " +
                "public String run(String param) { System.out.println(\"hello \" + param); return param; } " +
                "}";
        GroovyScriptEngin.callGroovyScript(groovyScript, "run", "world");
    }
}
