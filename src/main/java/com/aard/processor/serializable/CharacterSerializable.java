package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Character Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class CharacterSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = Character.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 25;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(Character value) {
        return serializableValue(value);
    }

    /**
     * 值字节长度
     *
     * @param value 值
     * @return int 长度
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/6 9:11
     */
    public static int valueByteLength(Character value) {
        if (value == null) {
            return 1;
        }
        return 3;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(Character value) {
        if (value == null) {
            return new byte[]{0};
        }
        byte[] bytes = new byte[3];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setShort(bytes, 1, value);
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
    public static void serializableValue(Character value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        buffer.writeByte(TYPE);
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
    public static int serializableValue(byte[] bytes, int index, Character value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        HeapByteBufUtil.setShort(bytes, index + 1, value);
        return index + 3;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Character
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static Character deserialization(ByteBuffer byteBuffer) {
        return byteBuffer.getChar();
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Character
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static Character deserializationValue(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        if (b == 0) {
            return null;
        }
        if (b != TYPE) {
            throw new SerializableException("The serialized type is inconsistent with the current type : " + b);
        }
        return byteBuffer.getChar();
    }


}
