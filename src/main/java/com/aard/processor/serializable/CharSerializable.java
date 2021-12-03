package com.aard.processor.serializable;

import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * char Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class CharSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = char.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 27;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(char value) {
        byte[] bytes = new byte[3];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setShort(bytes, 1, value);
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
    public static int valueByteLength(char value) {
        return 2;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(char value) {
        byte[] bytes = new byte[2];
        HeapByteBufUtil.setShort(bytes, 0, value);
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
    public static void serializableValue(char value, ByteBuf buffer) {
        buffer.writeChar(value);
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
    public static int serializableValue(byte[] bytes, int index, char value) {
        HeapByteBufUtil.setShort(bytes, index, value);
        return index + 2;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return char
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static char deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getChar();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return short
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static char deserializationValue(ByteBuffer byteBuffer) {
        return byteBuffer.getChar();
    }


}
