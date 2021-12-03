package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * int Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class IntSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = int.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 7;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(int value) {
        byte[] bytes = new byte[5];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setInt(bytes, 1, value);
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
    public static int valueByteLength(int value) {
        return 4;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(int value) {
        byte[] bytes = new byte[4];
        HeapByteBufUtil.setInt(bytes, 0, value);
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
    public static void serializableValue(int value, ByteBuf buffer) {
        buffer.writeInt(value);
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
    public static int serializableValue(byte[] bytes, int index, int value) {
        HeapByteBufUtil.setInt(bytes, index, value);
        return index + 4;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Integer
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static int deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Integer
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static int deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }


}
