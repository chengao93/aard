package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * short[] Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class ShoArraySerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = short[].class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 24;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(short[] value) {
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
    public static int valueByteLength(short[] value) {
        if (value == null) {
            return 1;
        }
        return 5 + (value.length << 1);
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(short[] value) {
        if (value == null) {
            return new byte[]{0};
        }
        int len = 5 + (value.length << 1);
        byte[] bytes = new byte[len];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setInt(bytes, 1, value.length);
        int index = 5;
        for (short val : value) {
            index = ShoSerializable.serializableValue(bytes, index, val);
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
    public static void serializableValue(short[] value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        int len = valueByteLength(value);
        buffer.ensureWritable(len);
        int index = buffer.writerIndex();
        byte[] array = buffer.array();
        index = serializableValue(array, index, value);
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
    public static int serializableValue(byte[] bytes, int index, short[] value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index += 1;
        HeapByteBufUtil.setInt(bytes, index, value.length);
        index += 4;
        for (short val : value) {
            index = ShoSerializable.serializableValue(bytes, index, val);
        }
        return index;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static short[] deserialization(ByteBuffer byteBuffer) {
        int len = byteBuffer.getInt();
        short[] arr = new short[len];
        for (int i = 0; i < len; i++) {
            arr[i] = byteBuffer.getShort();
        }
        return arr;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String[]
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static short[] deserializationValue(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        if (b == 0) {
            return null;
        }
        if (b != TYPE) {
            throw new SerializableException("The serialized type is inconsistent with the current type : " + b);
        }
        return deserialization(byteBuffer);
    }


}
