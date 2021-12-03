package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * boolean Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class BoolSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = boolean.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 31;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(boolean value) {
        byte[] bytes = new byte[2];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setByte(bytes, 1, value ? 1 : 0);
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
    public static int valueByteLength(boolean value) {
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
    public static byte[] serializableValue(boolean value) {
        byte[] bytes = new byte[1];
        HeapByteBufUtil.setByte(bytes, 0, value ? 1 : 0);
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
    public static void serializableValue(boolean value, ByteBuf buffer) {
        buffer.writeBoolean(value);
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
    public static int serializableValue(byte[] bytes, int index, boolean value) {
        HeapByteBufUtil.setByte(bytes, index, value ? 1 : 0);
        return index + 1;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static boolean deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.get() == 1;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static boolean deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.get() == 1;
    }


}
