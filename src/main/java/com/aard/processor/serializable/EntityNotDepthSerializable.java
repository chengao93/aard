package com.aard.processor.serializable;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.EntityService;
import com.aard.processor.util.ClassUtil;
import com.aard.processor.util.HeapByteBufUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Entity Serializable
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/20 12:13
 */
public class EntityNotDepthSerializable {

    /**
     * 类型标识
     */
    public static final byte TYPE = 51;

    /**
     * 序列化
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializable(EntityService value) {
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
    public static int valueByteLength(EntityService value) {
        if (value == null) {
            return 1;
        }
        int len = 1 + 5;
        String className = value.getClass().getName();
        len += (className.length() << 1);
        return value.byteLengthNotEntity() + len;
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return byte
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(EntityService value) {
        if (value == null) {
            return new byte[]{0};
        }
        int len = valueByteLength(value);
        byte[] bytes = new byte[len];
        HeapByteBufUtil.setByte(bytes, 0, TYPE);
        int index = StringSerializable.serializableValue(bytes, 1, value.getClass().getName());
        value.serializableNotEntity(bytes, index);
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
    public static void serializableValue(EntityService value, ByteBuf buffer) {
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
    public static int serializableValue(byte[] bytes, int index, EntityService value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        HeapByteBufUtil.setByte(bytes, index, TYPE);
        index = StringSerializable.serializableValue(bytes, index + 1, value.getClass().getName());
        index = value.serializableNotEntity(bytes, index);
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
    public static EntityService deserialization(ByteBuffer byteBuffer) {
        String className = StringSerializable.deserializationValue(byteBuffer);
        EntityService entityService;
        try {
            entityService = (EntityService) ClassUtil.getInstance(className);
        } catch (Exception e) {
            throw new SerializableException(e.getMessage());
        }
        entityService.deserializationNotEntity(byteBuffer);
        return entityService;
    }

    /**
     * 反序列化值
     *
     * @param byteBuffer 缓冲流
     * @return Collection
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static EntityService deserializationValue(ByteBuffer byteBuffer) {
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
