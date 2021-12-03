package com.aard.processor.util;

import java.nio.ByteBuffer;

/**
 * Aard 序列化
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/21 23:07
 */
public class AardSerializer {
    /**
     * 对象转字节流
     *
     * @param obj 对象
     * @return byte[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 23:08
     */
    public static byte[] toBytesNotDepth(Object obj) {
        return ObjectSerializableUtil.serializableNotDepth(obj);
    }

    /**
     * 字节流反序例化对象
     *
     * @param bytes 字节流
     * @return Object
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/21 23:08
     */
    public static <T> T parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return (T) ObjectSerializableUtil.deserializationValue(buffer);
    }
}
