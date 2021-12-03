package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * String Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class StringSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = String.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 1;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(String value) {
        if (value == null) {
            return new byte[]{0};
        }
        final int length = value.length();
        int index = 0;
        byte[] array = new byte[1 + (length << 1)];
        HeapByteBufUtil.setByte(array, index, TYPE);
        index += 1;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(array, index, value.charAt(i));
            index += 2;
        }
        return array;
    }

    /**
     * 值字节长度
     *
     * @param value 值
     * @return int 长度
     * @author chengao chengao163postbox@163.com
     * @date 2021/11/6 9:11
     */
    public static int valueByteLength(String value) {
        if (value == null) {
            return 1;
        }
        final int length = value.length();
        return 5 + (length << 1);
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(String value) {
        if (value == null) {
            return new byte[]{0};
        }
        final int length = value.length();
        int index = 0;
        byte[] array = new byte[5 + (length << 1)];
        HeapByteBufUtil.setByte(array, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(array, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(array, index, value.charAt(i));
            index += 2;
        }
        return array;
    }

    /**
     * 序列化值
     *
     * @param value  值
     * @param buffer 缓冲流
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static void serializableValue(String value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        final int length = value.length();
        buffer.ensureWritable(5 + (length << 1));
        int index = buffer.writerIndex();
        byte[] array = buffer.array();
        HeapByteBufUtil.setByte(array, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(array, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(array, index, value.charAt(i));
            index += 2;
        }
        buffer.writerIndex(index);
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
    public static int serializableValue(byte[] bytes, int index, String value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        final int length = value.length();
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(bytes, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(bytes, index, value.charAt(i));
            index += 2;
        }
        return index;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static String deserialization(ByteBuffer byteBuffer) {
        int length = byteBuffer.capacity() - byteBuffer.position();
        length = length >> 1;
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = byteBuffer.getChar();
        }
        return new String(chars, 0, length);

    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static String deserializationValue(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        if (b == 0) {
            return null;
        }
        if (b != TYPE) {
            throw new SerializableException("The serialized type is inconsistent with the current type : " + b);
        }
        int length = byteBuffer.getInt();
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = byteBuffer.getChar();
        }
        return new String(chars, 0, length);
    }

}
