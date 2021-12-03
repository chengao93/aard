package com.aard.processor;

import com.aard.processor.function.GetMethod;
import com.aard.processor.function.SetMethod;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Entity 通用服务
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/9 21:08
 */
public interface EntityService {
    /**
     * getter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] getterFields();

    /**
     * getter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] getterNotEntityFields();

    /**
     * getter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] getterEntityFields();

    /**
     * setter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] setterFields();

    /**
     * setter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] setterNotEntityFields();

    /**
     * setter field 字段
     *
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    String[] setterEntityFields();

    /**
     * getter methods 方法
     *
     * @return GetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod[] getterMethods();

    /**
     * getter methods 方法
     *
     * @return GetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod[] getterNotEntityMethods();

    /**
     * getter methods 方法
     *
     * @return GetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod[] getterEntityMethods();

    /**
     * setter methods 方法
     *
     * @return SetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod[] setterMethods();

    /**
     * setter methods 方法
     *
     * @return SetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod[] setterNotEntityMethods();

    /**
     * setter methods 方法
     *
     * @return SetMethod[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod[] setterEntityMethods();

    /**
     * getter methods 方法
     *
     * @param field 字段名
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterMethod(String field);

    /**
     * getter methods 方法
     *
     * @param field 字段名
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterNotEntityMethod(String field);

    /**
     * getter methods 方法
     *
     * @param field 字段名
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterEntityMethod(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterMethod(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterNotEntityMethod(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterEntityMethod(String field);

    /**
     * getter methods 方法
     *
     * @param field 字段名 如：getName形式
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterMethod2(String field);

    /**
     * getter methods 方法
     *
     * @param field 字段名 如：getName形式
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterNotEntityMethod2(String field);

    /**
     * getter methods 方法
     *
     * @param field 字段名 如：getName形式
     * @return GetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    GetMethod getterEntityMethod2(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名 如：setName形式
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterMethod2(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名 如：setName形式
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterNotEntityMethod2(String field);

    /**
     * setter methods 方法
     *
     * @param field 字段名 如：setName形式
     * @return SetMethod
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 16:25
     */
    @SuppressWarnings("all")
    SetMethod setterEntityMethod2(String field);

    /**
     * 是否存在getter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterField(String field);

    /**
     * 是否存在getter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterNotEntityField(String field);

    /**
     * 是否存在getter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterEntityField(String field);

    /**
     * 是否存在setter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterField(String field);

    /**
     * 是否存在setter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterNotEntityField(String field);

    /**
     * 是否存在setter field
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterEntityField(String field);

    /**
     * 是否存在getter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterField2(String field);

    /**
     * 是否存在getter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterNotEntityField2(String field);

    /**
     * 是否存在getter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasGetterEntityField2(String field);

    /**
     * 是否存在setter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterField2(String field);

    /**
     * 是否存在setter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterNotEntityField2(String field);

    /**
     * 是否存在setter field method name
     *
     * @param field field
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 21:16
     */
    boolean hasSetterEntityField2(String field);

    /**
     * 执行 getter field 方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterField(String field);

    /**
     * 执行 getter field 方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterNotEntityField(String field);

    /**
     * 执行 getter field 方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterEntityField(String field);

    /**
     * 执行 setter field 方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterField(String field, Object value);

    /**
     * 执行 setter field 方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterNotEntityField(String field, Object value);

    /**
     * 执行 setter field 方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterEntityField(String field, Object value);

    /**
     * 执行 getter field method name  方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterField2(String field);

    /**
     * 执行 getter field method name  方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterNotEntityField2(String field);

    /**
     * 执行 getter field method name  方法
     *
     * @param field field
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    Object invokedGetterEntityField2(String field);

    /**
     * 执行 setter field method name  方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterField2(String field, Object value);

    /**
     * 执行 setter field method name  方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterNotEntityField2(String field, Object value);

    /**
     * 执行 setter field method name 方法
     *
     * @param field field
     * @param value value
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/14 22:54
     */
    void invokedSetterEntityField2(String field, Object value);

    /**
     * 实例字节长度
     *
     * @return int 实例字节长度
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    int byteLengthNotEntity();

    /**
     * 执行序列化
     *
     * @param buffer 缓中流
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    void serializableNotEntity(ByteBuf buffer);

    /**
     * 执行序列化
     *
     * @param bytes 缓中流
     * @param index 开始位置
     * @return int
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    int serializableNotEntity(byte[] bytes, int index);

    /**
     * 执行反序列化
     *
     * @param buffer 缓中流
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    void deserializationNotEntity(ByteBuffer buffer);

    /**
     * 复制属性值
     *
     * @param target 目标对象
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    void copy(Object target);

    /**
     * 复制属性值
     *
     * @param target 目标对象
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 20:44
     */
    void copyNotEntity(Object target);

}
