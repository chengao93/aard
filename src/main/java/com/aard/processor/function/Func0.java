package com.aard.processor.function;

/**
 * 函数
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/12 20:26
 */
@FunctionalInterface
public interface Func0<R> {
    /**
     * 执行函数
     *
     * @return R
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/13 20:07
     */
    R apply();
}
