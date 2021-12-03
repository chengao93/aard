package com.aard.processor.function;

/**
 * get 方法
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/11/20 20:22
 */
public interface GetMethod<T, R> {
    /**
     * 执行get
     *
     * @param item 对像
     * @return R
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/20 20:22
     */
    R get(T item);

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
