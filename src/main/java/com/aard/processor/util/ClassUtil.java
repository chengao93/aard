package com.aard.processor.util;

import com.aard.processor.InstanceService;
import com.aard.processor.exception.ClassInitException;
import com.aard.processor.DefaultInstanceServiceImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类工具
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/22 10:06
 */
public class ClassUtil {

    private static final Map<String, InstanceService> CLASS_NAME_INSTANCE_SERVICE = new ConcurrentHashMap<>();

    /**
     * 通过类名获取实例
     *
     * @param classes 类
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:08
     */
    public static <T> T getInstance(Class<T> classes) {
        return (T) getInstance(classes.getName());
    }

    /**
     * 通过类名获取实例
     *
     * @param className 类名
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:08
     */
    public static Object getInstance(String className) {
        InstanceService instanceService = CLASS_NAME_INSTANCE_SERVICE.get(className);
        if (instanceService != null) {
            return instanceService.newInstance();
        }
        try {
            ClassPool cp = new ClassPool(true);
            cp.insertClassPath(new ClassClassPath(InstanceService.class));
            CtClass cc = cp.makeClass("$" + className + "$InstanceServiceImpl");
            cc.setSuperclass(cp.get(DefaultInstanceServiceImpl.class.getName()));
            cc.addInterface(cp.get(InstanceService.class.getName()));
            StringBuilder methodBuilder = new StringBuilder();
            methodBuilder.append("public Object newInstance() {");
            methodBuilder.append("return new ").append(className).append("();");
            methodBuilder.append("}");
            cc.addMethod(CtMethod.make(methodBuilder.toString(), cc));
            Class classes = cc.toClass();
            instanceService = (InstanceService) classes.getConstructor().newInstance();
            CLASS_NAME_INSTANCE_SERVICE.put(className, instanceService);
            return instanceService.newInstance();
        } catch (Exception e) {
            throw new ClassInitException(e.getMessage());
        }
    }

    private static final Map<String, InstanceService> CLASS_NAME_INSTANCE_SERVICE_INT_PARAM = new ConcurrentHashMap<>();

    /**
     * 通过类名获取实例
     *
     * @param classes 类
     * @param param1  参数
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:08
     */
    public static <T> T getInstance(Class<T> classes, int param1) {
        return (T) getInstance(classes.getName(), param1);
    }

    /**
     * 通过类名获取实例
     *
     * @param className 类名
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:08
     */
    public static Object getInstance(String className, int param1) {
        InstanceService instanceService = CLASS_NAME_INSTANCE_SERVICE_INT_PARAM.get(className);
        if (instanceService != null) {
            return instanceService.newInstance(param1);
        }
        try {
            ClassPool cp = new ClassPool(true);
            cp.insertClassPath(new ClassClassPath(InstanceService.class));
            CtClass cc = cp.makeClass("$" + className + "$Int$InstanceServiceImpl");
            cc.setSuperclass(cp.get(DefaultInstanceServiceImpl.class.getName()));
            cc.addInterface(cp.get(InstanceService.class.getName()));
            StringBuilder methodBuilder = new StringBuilder();
            methodBuilder.append("public Object newInstance(int param1) {");
            methodBuilder.append("return new ").append(className).append("(param1);");
            methodBuilder.append("}");
            cc.addMethod(CtMethod.make(methodBuilder.toString(), cc));
            Class classes = cc.toClass();
            instanceService = (InstanceService) classes.getConstructor().newInstance();
            CLASS_NAME_INSTANCE_SERVICE_INT_PARAM.put(className, instanceService);
            return instanceService.newInstance(param1);
        } catch (Exception e) {
            throw new ClassInitException(e.getMessage());
        }
    }

    /**
     * 检查目标类是否可以从原类转化<br>
     * 转化包括：<br>
     * 1、原类是对象，目标类型是原类型实现的接口<br>
     * 2、目标类型是原类型的父类<br>
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param targetType 目标类型
     * @param sourceType 原类型
     * @return 是否可转化
     */
    public static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        if (null == targetType || null == sourceType) {
            return false;
        }

        // 对象类型
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // 基本类型
        if (targetType.isPrimitive()) {
            // 原始类型
            Class<?> resolvedPrimitive = BasicType.wrapperPrimitiveMap.get(sourceType);
            if (resolvedPrimitive != null && targetType.equals(resolvedPrimitive)) {
                return true;
            }
        } else {
            // 包装类型
            Class<?> resolvedWrapper = BasicType.primitiveWrapperMap.get(sourceType);
            if (resolvedWrapper != null && targetType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }
}
