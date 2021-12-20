package com.aard.processor.serializable;

import com.aard.processor.exception.ClassInitException;
import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.ClassUtil;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Class Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class ClassSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = Class.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 53;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(Class value) {
        if (value == null) {
            return new byte[]{0};
        }
        String name = value.getName();
        int length = name.length();
        byte[] bytes = new byte[1 + length << 1];
        int index = 0;
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index += 1;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(bytes, index, name.charAt(i));
            index += 2;
        }
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
    public static int valueByteLength(Class value) {
        if (value == null) {
            return 1;
        }
        String name = value.getName();
        return StringSerializable.valueByteLength(name);
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(Class value) {
        if (value == null) {
            return new byte[]{0};
        }
        String name = value.getName();
        int length = name.length();
        byte[] bytes = new byte[1 + length << 1];
        int index = 0;
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(bytes, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(bytes, index, name.charAt(i));
            index += 2;
        }
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
    public static void serializableValue(Class value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        String name = value.getName();
        final int length = name.length();
        buffer.ensureWritable(5 + (length << 1));
        int index = buffer.writerIndex();
        byte[] array = buffer.array();
        HeapByteBufUtil.setByte(array, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(array, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(array, index, name.charAt(i));
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
    public static int serializableValue(byte[] bytes, int index, Class value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        String name = value.getName();
        final int length = name.length();
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(bytes, index, length);
        index += 4;
        for (int i = 0; i < length; i++) {
            HeapByteBufUtil.setShort(bytes, index, name.charAt(i));
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
    public static Class deserialization(ByteBuffer byteBuffer) {
        int length = byteBuffer.capacity() - byteBuffer.position();
        length = length >> 1;
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = byteBuffer.getChar();
        }
        String name = new String(chars, 0, length);
        try {
            return ClassUtil.getInstance(name).getClass();
        } catch (Exception e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException classNotFoundException) {
                throw new ClassInitException(classNotFoundException.getMessage());
            }
        }
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static Class deserializationValue(ByteBuffer byteBuffer) {
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
        String name = new String(chars, 0, length);
        try {
            return ClassUtil.getInstance(name).getClass();
        } catch (Exception e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException classNotFoundException) {
                throw new ClassInitException(classNotFoundException.getMessage());
            }
        }
    }


}
