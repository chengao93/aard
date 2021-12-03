package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * byte Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class BytSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = byte.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 35;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(byte value) {
        byte[] bytes = new byte[2];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setByte(bytes, 1, value);
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
    public static int valueByteLength(byte value) {
        return 1;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(byte value) {
        byte[] bytes = new byte[1];
        HeapByteBufUtil.setByte(bytes, 0, value);
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
    public static void serializableValue(byte value, ByteBuf buffer) {
        buffer.writeByte(value);
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
    public static int serializableValue(byte[] bytes, int index, byte value) {
        HeapByteBufUtil.setByte(bytes, index, value);
        return index + 1;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }


}
