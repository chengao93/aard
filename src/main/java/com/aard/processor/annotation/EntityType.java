package com.aard.processor.annotation;

import java.lang.annotation.*;

/**
 * ElementType 标记，在类中生成Entity通用方法
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/7 11:08
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface EntityType {
    Class classes() default Object.class;

    boolean parent() default true;
}
