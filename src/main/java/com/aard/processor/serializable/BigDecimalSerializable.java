package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * BigDecimal Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class BigDecimalSerializable {
    /**
     * 类型名称
     */
    public static final String CLASS_NAME = BigDecimal.class.getName();

    /**
     * 类型标识
     */
    public static final byte TYPE = 3;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(BigDecimal value) {
        if (value == null) {
            return new byte[]{0};
        }
        BigInteger bigInteger = value.unscaledValue();
        int byteLen = bigInteger.bitLength() / 8 + 1 + 2;
        byte[] bytes = new byte[byteLen];
        byte[] array = bigInteger.toByteArray();
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setByte(bytes, 1, value.scale());
        HeapByteBufUtil.setBytes(bytes, 2, array);
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
    public static int valueByteLength(BigDecimal value) {
        if (value == null) {
            return 1;
        }
        return value.unscaledValue().bitLength() / 8 + 1 + 3;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(BigDecimal value) {
        if (value == null) {
            return new byte[]{0};
        }
        BigInteger bigInteger = value.unscaledValue();
        int byteLen = bigInteger.bitLength() / 8 + 1 + 3;
        byte[] bytes = new byte[byteLen];
        byte[] array = bigInteger.toByteArray();
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        HeapByteBufUtil.setByte(bytes, 1, array.length);
        HeapByteBufUtil.setByte(bytes, 2, value.scale());
        HeapByteBufUtil.setBytes(bytes, 3, array);
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
    public static void serializableValue(BigDecimal value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        BigInteger bigInteger = value.unscaledValue();
        int len = bigInteger.bitLength() / 8 + 1 + 3;
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
    public static int serializableValue(byte[] bytes, int index, BigDecimal value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        BigInteger bigInteger = value.unscaledValue();
        int len = bigInteger.bitLength() / 8 + 1 + 3;
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        byte[] array = bigInteger.toByteArray();
        HeapByteBufUtil.setByte(bytes, index + 1, array.length);
        HeapByteBufUtil.setByte(bytes, index + 2, value.scale());
        HeapByteBufUtil.setBytes(bytes, index + 3, array);
        return index + len;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return BigDecimal
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static BigDecimal deserialization(ByteBuffer byteBuffer) {
        int scale = byteBuffer.get();
        byte[] bytes = new byte[byteBuffer.capacity() - byteBuffer.position()];
        byteBuffer.get(bytes, 0, bytes.length);
        return new BigDecimal(new BigInteger(bytes), scale);
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return BigDecimal
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static BigDecimal deserializationValue(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        if (b == 0) {
            return null;
        }
        if (b != TYPE) {
            throw new SerializableException("The serialized type is inconsistent with the current type : " + b);
        }
        int length = byteBuffer.get();
        int scale = byteBuffer.get();
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes, 0, bytes.length);
        return new BigDecimal(new BigInteger(bytes), scale);
    }


}
