package com.aard.processor.util;

import com.aard.asm.*;
import com.aard.processor.InstanceService;
import com.aard.processor.exception.ClassInitException;
import com.aard.processor.DefaultInstanceServiceImpl;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类工具
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/22 10:06
 */
public class ClassUtil implements Opcodes {

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
        synchronized (CLASS_NAME_INSTANCE_SERVICE) {
            if (CLASS_NAME_INSTANCE_SERVICE.containsKey(className)) {
                return CLASS_NAME_INSTANCE_SERVICE.get(className).newInstance();
            }
            try {
                String prefix = "";
                if (StringUtils.startsWith(className, "java.")) {
                    prefix = "$";
                }
                String implClassName = prefix + className + "$InstanceServiceImpl";
                ClassLoader classLoader = Class.forName(className).getClassLoader();
                if (classLoader == null) {
                    classLoader = ClassUtil.class.getClassLoader();
                }
                byte[] bytes = createInstanceImplClass(implClassName, className);
                Method method = AardReflectUtil.getMethod(classLoader.getClass(), "defineClass", String.class, byte[].class, int.class, int.class);
                if (false == method.isAccessible()) {
                    method.setAccessible(true);
                }
                Class<?> classes = (Class<?>) method.invoke(classLoader, null, bytes, 0, bytes.length);
                instanceService = (InstanceService) classes.getConstructor().newInstance();
                CLASS_NAME_INSTANCE_SERVICE.put(className, instanceService);
                return instanceService.newInstance();
            } catch (Exception e) {
                throw new ClassInitException(e.getMessage());
            }
        }
    }

    /**
     * 创建实现类
     *
     * @param implClassName     类名
     * @param instanceClassName 实例类名
     * @return byte[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/12/9 17:59
     */
    private static byte[] createInstanceImplClass(String implClassName, String instanceClassName) {

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        String implClassNamePath = classPath(implClassName);
        String instanceClassNamePath = classPath(instanceClassName);

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, implClassNamePath, null, classPath(DefaultInstanceServiceImpl.class), new String[]{classPath(InstanceService.class)});

        String className = implClassName.substring(implClassName.indexOf(".") + 1);
        cw.visitSource(className + ".java", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(7, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, classPath(DefaultInstanceServiceImpl.class), "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + implClassNamePath + ";", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "()L" + classPath(Object.class) + ";", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(10, l0);
            mv.visitTypeInsn(NEW, instanceClassNamePath);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, instanceClassNamePath, "<init>", "()V", false);
            mv.visitInsn(ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + implClassNamePath + ";", null, l0, l1, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
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
        synchronized (CLASS_NAME_INSTANCE_SERVICE_INT_PARAM) {
            if (CLASS_NAME_INSTANCE_SERVICE_INT_PARAM.containsKey(className)) {
                return CLASS_NAME_INSTANCE_SERVICE_INT_PARAM.get(className).newInstance();
            }
            try {
                String prefix = "";
                if (StringUtils.startsWith(className, "java.")) {
                    prefix = "$";
                }
                String implClassName = prefix + className + "$Int$InstanceServiceImpl";
                ClassLoader classLoader = Class.forName(className).getClassLoader();
                if (classLoader == null) {
                    classLoader = ClassUtil.class.getClassLoader();
                }
                byte[] bytes = createInstanceImplClassParam1(implClassName, className);
                Method method = classLoader.getClass().getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                if (false == method.isAccessible()) {
                    method.setAccessible(true);
                }
                Class<?> classes = (Class<?>) method.invoke(classLoader, null, bytes, 0, bytes.length);
                instanceService = (InstanceService) classes.getConstructor().newInstance();
                CLASS_NAME_INSTANCE_SERVICE_INT_PARAM.put(className, instanceService);
                return instanceService.newInstance(param1);
            } catch (Exception e) {
                throw new ClassInitException(e.getMessage());
            }
        }
    }

    /**
     * 创建实现类
     *
     * @param implClassName     类名
     * @param instanceClassName 实例类名
     * @return byte[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/12/9 17:59
     */
    private static byte[] createInstanceImplClassParam1(String implClassName, String instanceClassName) {

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        String implClassNamePath = classPath(implClassName);
        String instanceClassNamePath = classPath(instanceClassName);

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, implClassNamePath, null, classPath(DefaultInstanceServiceImpl.class), new String[]{classPath(InstanceService.class)});

        String className = implClassName.substring(implClassName.indexOf(".") + 1);
        cw.visitSource(className + ".java", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(7, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, classPath(DefaultInstanceServiceImpl.class), "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + implClassNamePath + ";", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "(I)L" + classPath(Object.class) + ";", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(12, l0);
            mv.visitTypeInsn(NEW, instanceClassNamePath);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, instanceClassNamePath, "<init>", "(I)V", false);
            mv.visitInsn(ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + implClassNamePath + ";", null, l0, l1, 0);
            mv.visitLocalVariable("param", "I", null, l0, l1, 1);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
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

    /**
     * 类路径
     *
     * @param classes 类
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/12/9 17:46
     */
    private static String classPath(Class classes) {
        return classPath(classes.getName());
    }

    /**
     * 类名路径
     *
     * @param className 类名
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/12/9 17:45
     */
    private static String classPath(String className) {
        return className.replace(".", "/");
    }

}
