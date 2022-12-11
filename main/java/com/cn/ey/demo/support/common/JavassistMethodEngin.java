package com.cn.ey.demo.support.common;

import cn.hutool.crypto.digest.MD5;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class JavassistMethodEngin {
    private static final ClassPool CLASS_POLL = ClassPool.getDefault();

    public static Object callJavaMethod(String function, String method, Object param)
            throws CannotCompileException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String key = MD5.create().digestHex(function);
        CtClass ctClass = CLASS_POLL.makeClass(key);

        CtMethod ctMethod = CtMethod.make(function, ctClass);
        ctClass.addMethod(ctMethod);

        //CtField cf1 = CtField.make("private int id;", ctClass);
        //ccs.addField(cf1);

        //CtConstructor ctConstructor = new CtConstructor(new CtClass[]{ CtClass.intType, CLASS_POLL.get("java.lang.String") }, ctClass);
        //ctConstructor.setBody("{this.id = $1; this.name=$2;}"); // $1表示第一个参数，$2表示第二个参数
        //ctClass.addConstructor(ctConstructor);

        Class<?> clazz = ctClass.toClass();

        Object o = clazz.getConstructor().newInstance();
        if (param != null) {
            Class<?> pclazz = param.getClass();
            Method insertMethod = clazz.getDeclaredMethod(method, pclazz);
            return insertMethod.invoke(o, param);
        }
        Method insertMethod = clazz.getDeclaredMethod(method);
        return insertMethod.invoke(o);
    }

    public static void main(String[] args)
            throws CannotCompileException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Map<String, String> param = new HashMap<>();
        param.put("key", "value");
        JavassistMethodEngin.callJavaMethod(
                "public void call(java.util.HashMap param) {System.out.println(param); } ",
                "call", param);
    }
}
