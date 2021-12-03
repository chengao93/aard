package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.util.ClassUtil;
import com.aard.processor.util.HeapByteBufUtil;
import com.aard.processor.util.ObjectSerializableUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * String[] Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class MapSerializable {

    /**
     * 类型标识
     */
    public static final byte TYPE = 50;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(Map value) {
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
    public static int valueByteLength(Map value) {
        if (value == null) {
            return 1;
        }
        int len = 1 + 4 + 5;
        for (Object obj : value.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            len += ObjectSerializableUtil.valueByteLengthNotDepth(entry.getKey());
            len += ObjectSerializableUtil.valueByteLengthNotDepth(entry.getValue());
        }
        return len;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(Map value) {
        if (value == null) {
            return new byte[]{0};
        }
        int len = valueByteLength(value);
        byte[] bytes = new byte[len];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        int index = StringSerializable.serializableValue(bytes, 1, value.getClass().getName());
        HeapByteBufUtil.setInt(bytes, index, value.size());
        index += 4;
        for (Object obj : value.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            index = ObjectSerializableUtil.serializableValue(bytes, index, entry.getKey());
            index = ObjectSerializableUtil.serializableValue(bytes, index, entry.getValue());
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
    public static void serializableValue(Map value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        int len = valueByteLength(value);
        buffer.ensureWritable(len);
        int index = buffer.writerIndex();
        byte[] bytes = buffer.array();
        index = serializableValue(bytes, index, value);
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
    public static int serializableValue(byte[] bytes, int index, Map value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index = StringSerializable.serializableValue(bytes, index + 1, value.getClass().getName());
        HeapByteBufUtil.setInt(bytes, index, value.size());
        index += 4;
        for (Object obj : value.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            index = ObjectSerializableUtil.serializableValue(bytes, index, entry.getKey());
            index = ObjectSerializableUtil.serializableValue(bytes, index, entry.getValue());
        }
        return index;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Collection
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static Map deserialization(ByteBuffer byteBuffer) {
        String className = StringSerializable.deserializationValue(byteBuffer);
        int size = byteBuffer.getInt();
        Map map;
        try {
            map = (Map) ClassUtil.getInstance(className, size);
        } catch (Exception e) {
            throw new SerializableException(e.getMessage());
        }
        for (int i = 0; i < size; i++) {
            Object key = ObjectSerializableUtil.deserializationValue(byteBuffer);
            Object value = ObjectSerializableUtil.deserializationValue(byteBuffer);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Collection
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static Map deserializationValue(ByteBuffer byteBuffer) {
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
