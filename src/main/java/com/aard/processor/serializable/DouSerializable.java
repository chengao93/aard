package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * double Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class DouSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = double.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 11;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(double value) {
        byte[] bytes = new byte[9];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setLong(bytes, 1, Double.doubleToRawLongBits(value));
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
    public static int valueByteLength(double value) {
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
    public static byte[] serializableValue(double value) {
        byte[] bytes = new byte[8];
        HeapByteBufUtil.setLong(bytes, 0, Double.doubleToRawLongBits(value));
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
    public static void serializableValue(double value, ByteBuf buffer) {
        buffer.writeDouble(value);
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
    public static int serializableValue(byte[] bytes, int index, double value) {
        HeapByteBufUtil.setLong(bytes, index, Double.doubleToRawLongBits(value));
        return index + 2;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Integer
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static double deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getDouble();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Integer
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static double deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.getDouble();
    }


}
