package com.aard.processor;

/**
 * 实例服务实现
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/22 10:37
 */
public class DefaultInstanceServiceImpl implements InstanceService {
    /**
     * 创建实例
     *
     * @return Object 实例
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:04
     */
    @Override
    public Object newInstance() {//默认不实现
        return null;
    }

    /**
     * 创建实例
     *
     * @param param1 参数
     * @return Object 实例
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/22 10:04
     */
    @Override
    public Object newInstance(int param1) {
        return null;
    }
}
