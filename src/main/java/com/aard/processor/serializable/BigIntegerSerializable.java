package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * BigInteger Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class BigIntegerSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = BigInteger.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 47;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(BigInteger value) {
        if (value == null) {
            return new byte[]{0};
        }
        int byteLen = value.bitLength() / 8 + 1 + 1;
        byte[] bytes = new byte[byteLen];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setBytes(bytes, 1, value.toByteArray());
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
    public static int valueByteLength(BigInteger value) {
        if (value == null) {
            return 1;
        }
        return value.bitLength() / 8 + 1 + 2;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(BigInteger value) {
        if (value == null) {
            return new byte[]{0};
        }
        int byteLen = value.bitLength() / 8 + 1 + 2;
        byte[] bytes = new byte[byteLen];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        byte[] array = value.toByteArray();
        HeapByteBufUtil.setByte(bytes, 1, array.length);
        HeapByteBufUtil.setBytes(bytes, 2, array);
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
    public static void serializableValue(BigInteger value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        int len = value.bitLength() / 8 + 1 + 2;
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
    public static int serializableValue(byte[] bytes, int index, BigInteger value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        int len = value.bitLength() / 8 + 1 + 2;
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        byte[] array = value.toByteArray();
        HeapByteBufUtil.setByte(bytes, index + 1, array.length);
        HeapByteBufUtil.setBytes(bytes, index + 2, array);
        return index + len;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static BigInteger deserialization(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.capacity() - byteBuffer.position()];
        byteBuffer.get(bytes, 0, bytes.length);
        return new BigInteger(bytes);
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static BigInteger deserializationValue(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        if (b == 0) {
            return null;
        }
        if (b != TYPE) {
            throw new SerializableException("The serialized type is inconsistent with the current type : " + b);
        }
        int length = byteBuffer.get();
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes, 0, length);
        return new BigInteger(bytes);
    }


}
