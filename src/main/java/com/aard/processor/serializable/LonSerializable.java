package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * long Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class LonSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = long.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 15;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(long value) {
        byte[] bytes = new byte[9];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setLong(bytes, 1, value);
        return bytes;
    }

    /**
     * 值字节长度
     *
     * @param value 值
     * @return int 长度
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/6 9:11
     */
    public static int valueByteLength(long value) {
        return 8;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(long value) {
        byte[] bytes = new byte[8];
        HeapByteBufUtil.setLong(bytes, 0, value);
        return bytes;
    }

    /**
     * 序列化值
     *
     * @param value  值
     * @param buffer 缓冲流
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static void serializableValue(long value, ByteBuf buffer) {
        buffer.writeLong(value);
    }

    /**
     * 序列化值
     *
     * @param bytes 缓冲流
     * @param index 开始位置
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static int serializableValue(byte[] bytes, int index, long value) {
        HeapByteBufUtil.setLong(bytes, index, value);
        return index + 8;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return long
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static long deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getLong();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return long
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static long deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.getLong();
    }


}
