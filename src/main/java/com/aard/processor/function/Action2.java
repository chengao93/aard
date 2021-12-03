package com.aard.processor.function;

/**
 * 函数
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/12 20:26
 */
@FunctionalInterface
public interface Action2<T, K> {
    /**
     * 执行函数
     *
     * @param t 参数
     * @param k 参数
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/13 20:07
     */
    void apply(T t, K k);
}
