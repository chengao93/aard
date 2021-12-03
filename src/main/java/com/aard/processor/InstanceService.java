package com.aard.processor;

/**
 * 实例服务
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/22 10:03
 */
public interface InstanceService {
    /**
     * 创建实例
     *
     * @return Object 实例
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:04
     */
    Object newInstance();

    /**
     * 创建实例
     *
     * @param param1 参数
     * @return Object 实例
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:04
     */
    Object newInstance(int param1);
}
