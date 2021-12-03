package com.aard.processor.function;

/**
 * 函数
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/12 20:26
 */
@FunctionalInterface
public interface Func2<T, K, R> {
    /**
     * 执行函数
     *
     * @param t 参数
     * @param k 参数
     * @return R
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/13 20:07
     */
    R apply(T t, K k);
}
