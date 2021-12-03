package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * float Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class FloSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = float.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 19;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(float value) {
        byte[] bytes = new byte[5];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setInt(bytes, 1, Float.floatToRawIntBits(value));
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
    public static int valueByteLength(float value) {
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
    public static byte[] serializableValue(float value) {
        byte[] bytes = new byte[4];
        HeapByteBufUtil.setInt(bytes, 0, Float.floatToRawIntBits(value));
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
    public static void serializableValue(float value, ByteBuf buffer) {
        buffer.writeFloat(value);
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
    public static int serializableValue(byte[] bytes, int index, float value) {
        HeapByteBufUtil.setInt(bytes, index, Float.floatToRawIntBits(value));
        return index + 4;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return float
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static float deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getFloat();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return float
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static float deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.getFloat();
    }


}
