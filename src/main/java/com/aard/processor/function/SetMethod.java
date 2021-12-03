package com.aard.processor.function;

/**
 * set 方法
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/11/20 20:22
 */
public interface SetMethod<T, V> {
    /**
     * 执行set
     *
     * @param item  对像
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/20 20:22
     */
    void set(T item, V value);

    /**
     * 方法名
     *
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/27 18:00
     */
    String name();

    /**
     * 字段名
     *
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/27 18:00
     */
    String field();

    /**
     * 类型
     *
     * @return Class
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/27 18:00
     */
    @SuppressWarnings("all")
    Class classes();

    /**
     * Entity 类型
     *
     * @return Class
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/27 18:00
     */
    @SuppressWarnings("all")
    Class entityClasses();
}
