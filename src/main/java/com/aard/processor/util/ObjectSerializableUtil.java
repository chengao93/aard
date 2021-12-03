package com.aard.processor.util;

import com.aard.processor.exception.SerializableException;
import com.aard.processor.serializable.*;
import com.aard.processor.EntityService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 对像序列化
 *
 * @author chengao chengao163postbox@163.com
 * @date 2021/10/21 12:07
 */
public class ObjectSerializableUtil {

    /**
     * 序列化,非深度序列化
     *
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableNotDepth(Object value) {
        if (value == null) {
            return new byte[]{0};
        }
        if (value instanceof EntityService) {
            return EntityNotDepthSerializable.serializable((EntityService) value);
        }
        String className = value.getClass().getName();
        switch (className) {
            case "[I":
                return IntArraySerializable.serializable((int[]) value);
            case "[D":
                return DouArraySerializable.serializable((double[]) value);
            case "[B":
                return BytArraySerializable.serializable((byte[]) value);
            case "[J":
                return LonArraySerializable.serializable((long[]) value);
            case "[F":
                return FloArraySerializable.serializable((float[]) value);
            case "[Z":
                return BoolArraySerializable.serializable((boolean[]) value);
            case "[S":
                return ShoArraySerializable.serializable((short[]) value);
            case "[C":
                return CharArraySerializable.serializable((char[]) value);
            case "java.lang.Integer":
                return IntegerSerializable.serializable((Integer) value);
            case "[Ljava.lang.Integer;":
                return IntegerArraySerializable.serializable((Integer[]) value);
            case "java.math.BigDecimal":
                return BigDecimalSerializable.serializable((BigDecimal) value);
            case "[Ljava.math.BigDecimal;":
                return BigDecimalArraySerializable.serializable((BigDecimal[]) value);
            case "java.math.BigInteger":
                return BigIntegerSerializable.serializable((BigInteger) value);
            case "[Ljava.math.BigInteger;":
                return BigIntegerArraySerializable.serializable((BigInteger[]) value);
            case "java.lang.Byte":
                return ByteSerializable.serializable((Byte) value);
            case "[Ljava.lang.Byte;":
                return ByteArraySerializable.serializable((Byte[]) value);
            case "java.lang.Boolean":
                return BooleanSerializable.serializable((Boolean) value);
            case "[Ljava.lang.Boolean;":
                return BooleanArraySerializable.serializable((Boolean[]) value);
            case "java.lang.Character":
                return CharacterSerializable.serializable((Character) value);
            case "[Ljava.lang.Character;":
                return CharacterArraySerializable.serializable((Character[]) value);
            case "java.util.Date":
                return DateSerializable.serializable((Date) value);
            case "[Ljava.util.Date;":
                return DateArraySerializable.serializable((Date[]) value);
            case "java.lang.Double":
                return DoubleSerializable.serializable((Double) value);
            case "[Ljava.lang.Double;":
                return DoubleArraySerializable.serializable((Double[]) value);
            case "java.lang.Float":
                return FloatSerializable.serializable((Float) value);
            case "[Ljava.lang.Float;":
                return FloatArraySerializable.serializable((Float[]) value);
            case "java.lang.Long":
                return LongSerializable.serializable((Long) value);
            case "[Ljava.lang.Long;":
                return LongArraySerializable.serializable((Long[]) value);
            case "java.lang.Short":
                return ShortSerializable.serializable((Short) value);
            case "[Ljava.lang.Short;":
                return ShortArraySerializable.serializable((Short[]) value);
            case "java.sql.Date":
                return SqlDateSerializable.serializable((java.sql.Date) value);
            case "[Ljava.sql.Date;":
                return SqlDateArraySerializable.serializable((java.sql.Date[]) value);
            case "java.lang.String":
                return StringSerializable.serializable((String) value);
            case "[Ljava.lang.String;":
                return StringArraySerializable.serializable((String[]) value);
            case "java.lang.StringBuffer":
                return StringBufferSerializable.serializable((StringBuffer) value);
            case "[Ljava.lang.StringBuffer;":
                return StringBufferArraySerializable.serializable((StringBuffer[]) value);
            case "java.lang.StringBuilder":
                return StringBuilderSerializable.serializable((StringBuilder) value);
            case "[Ljava.lang.StringBuilder;":
                return StringBuilderArraySerializable.serializable((StringBuilder[]) value);
            case "java.sql.Timestamp":
                return TimestampSerializable.serializable((java.sql.Timestamp) value);
            case "[Ljava.sql.Timestamp;":
                return TimestampArraySerializable.serializable((java.sql.Timestamp[]) value);
            default:
                if (value instanceof Collection) {
                    return CollectionSerializable.serializable((Collection) value);
                } else if (value instanceof Map) {
                    return MapSerializable.serializable((Map) value);
                } else if (value instanceof EntityService) {
                    return EntityNotDepthSerializable.serializable((EntityService) value);
                }
                throw new SerializableException("An unsupported serialization type :" + className);
        }
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static byte[] serializableValue(Object value) {
        ByteBuf buffer = Unpooled.buffer();
        serializableValue(value, buffer);
        byte[] bytes = new byte[buffer.writerIndex()];
        buffer.getBytes(0, bytes);
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
    public static void serializableValue(Object value, ByteBuf buffer) {
        if (value == null) {
            buffer.writeByte(0);
            return;
        }
        String className = value.getClass().getName();
        switch (className) {
            case "[I":
                buffer.writeBytes(IntArraySerializable.serializableValue((int[]) value));
                break;
            case "[D":
                buffer.writeBytes(DouArraySerializable.serializableValue((double[]) value));
                break;
            case "[B":
                buffer.writeBytes(BytArraySerializable.serializableValue((byte[]) value));
                break;
            case "[J":
                buffer.writeBytes(LonArraySerializable.serializableValue((long[]) value));
                break;
            case "[F":
                buffer.writeBytes(FloArraySerializable.serializableValue((float[]) value));
                break;
            case "[Z":
                buffer.writeBytes(BoolArraySerializable.serializableValue((boolean[]) value));
                break;
            case "[S":
                buffer.writeBytes(ShoArraySerializable.serializableValue((short[]) value));
                break;
            case "[C":
                buffer.writeBytes(CharArraySerializable.serializableValue((char[]) value));
                break;
            case "java.lang.Integer":
                buffer.writeBytes(IntegerSerializable.serializableValue((Integer) value));
                break;
            case "[Ljava.lang.Integer;":
                buffer.writeBytes(IntegerArraySerializable.serializableValue((Integer[]) value));
                break;
            case "java.math.BigDecimal":
                buffer.writeBytes(BigDecimalSerializable.serializableValue((BigDecimal) value));
                break;
            case "[Ljava.math.BigDecimal;":
                buffer.writeBytes(BigDecimalArraySerializable.serializableValue((BigDecimal[]) value));
                break;
            case "java.math.BigInteger":
                buffer.writeBytes(BigIntegerSerializable.serializableValue((BigInteger) value));
                break;
            case "[Ljava.math.BigInteger;":
                buffer.writeBytes(BigIntegerArraySerializable.serializableValue((BigInteger[]) value));
                break;
            case "java.lang.Byte":
                buffer.writeBytes(ByteSerializable.serializableValue((Byte) value));
                break;
            case "[Ljava.lang.Byte;":
                buffer.writeBytes(ByteArraySerializable.serializableValue((Byte[]) value));
                break;
            case "java.lang.Boolean":
                buffer.writeBytes(BooleanSerializable.serializableValue((Boolean) value));
                break;
            case "[Ljava.lang.Boolean;":
                buffer.writeBytes(BooleanArraySerializable.serializableValue((Boolean[]) value));
                break;
            case "java.lang.Character":
                buffer.writeBytes(CharacterSerializable.serializableValue((Character) value));
                break;
            case "[Ljava.lang.Character;":
                buffer.writeBytes(CharacterArraySerializable.serializableValue((Character[]) value));
                break;
            case "java.util.Date":
                buffer.writeBytes(DateSerializable.serializableValue((Date) value));
                break;
            case "[Ljava.util.Date;":
                buffer.writeBytes(DateArraySerializable.serializableValue((Date[]) value));
                break;
            case "java.lang.Double":
                buffer.writeBytes(DoubleSerializable.serializableValue((Double) value));
                break;
            case "[Ljava.lang.Double;":
                buffer.writeBytes(DoubleArraySerializable.serializableValue((Double[]) value));
                break;
            case "java.lang.Float":
                buffer.writeBytes(FloatSerializable.serializableValue((Float) value));
                break;
            case "[Ljava.lang.Float;":
                buffer.writeBytes(FloatArraySerializable.serializableValue((Float[]) value));
                break;
            case "java.lang.Long":
                buffer.writeBytes(LongSerializable.serializableValue((Long) value));
                break;
            case "[Ljava.lang.Long;":
                buffer.writeBytes(LongArraySerializable.serializableValue((Long[]) value));
                break;
            case "java.lang.Short":
                buffer.writeBytes(ShortSerializable.serializableValue((Short) value));
                break;
            case "[Ljava.lang.Short;":
                buffer.writeBytes(ShortArraySerializable.serializableValue((Short[]) value));
                break;
            case "java.sql.Date":
                buffer.writeBytes(SqlDateSerializable.serializableValue((java.sql.Date) value));
                break;
            case "[Ljava.sql.Date;":
                buffer.writeBytes(SqlDateArraySerializable.serializableValue((java.sql.Date[]) value));
                break;
            case "java.lang.String":
                buffer.writeBytes(StringSerializable.serializableValue((String) value));
                break;
            case "[Ljava.lang.String;":
                buffer.writeBytes(StringArraySerializable.serializableValue((String[]) value));
                break;
            case "java.lang.StringBuffer":
                buffer.writeBytes(StringBufferSerializable.serializableValue((StringBuffer) value));
                break;
            case "[Ljava.lang.StringBuffer;":
                buffer.writeBytes(StringBufferArraySerializable.serializableValue((StringBuffer[]) value));
                break;
            case "java.lang.StringBuilder":
                buffer.writeBytes(StringBuilderSerializable.serializableValue((StringBuilder) value));
                break;
            case "[Ljava.lang.StringBuilder;":
                buffer.writeBytes(StringBuilderArraySerializable.serializableValue((StringBuilder[]) value));
                break;
            case "java.sql.Timestamp":
                buffer.writeBytes(TimestampSerializable.serializableValue((java.sql.Timestamp) value));
                break;
            case "[Ljava.sql.Timestamp;":
                buffer.writeBytes(TimestampArraySerializable.serializableValue((java.sql.Timestamp[]) value));
                break;
            default:
                if (value instanceof Collection) {
                    buffer.writeBytes(CollectionSerializable.serializableValue((Collection) value));
                    break;
                } else if (value instanceof Map) {
                    buffer.writeBytes(MapSerializable.serializableValue((Map) value));
                } else if (value instanceof EntityService) {
                    buffer.writeBytes(EntityNotDepthSerializable.serializableValue((EntityService) value));
                }
                throw new SerializableException("An unsupported serialization type :" + className);
        }
    }

    /**
     * 序列化值
     *
     * @param bytes 缓冲流
     * @param index 位置
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static int serializableValue(byte[] bytes, int index, Object value) {
        if (value == null) {
            HeapByteBufUtil.setByte(bytes, index, 0);
            return index + 1;
        }
        String className = value.getClass().getName();
        switch (className) {
            case "[I":
                return IntArraySerializable.serializableValue(bytes, index, (int[]) value);
            case "[D":
                return DouArraySerializable.serializableValue(bytes, index, (double[]) value);
            case "[B":
                return BytArraySerializable.serializableValue(bytes, index, (byte[]) value);
            case "[J":
                return LonArraySerializable.serializableValue(bytes, index, (long[]) value);
            case "[F":
                return FloArraySerializable.serializableValue(bytes, index, (float[]) value);
            case "[Z":
                return BoolArraySerializable.serializableValue(bytes, index, (boolean[]) value);
            case "[S":
                return ShoArraySerializable.serializableValue(bytes, index, (short[]) value);
            case "[C":
                return CharArraySerializable.serializableValue(bytes, index, (char[]) value);
            case "java.lang.Integer":
                return IntegerSerializable.serializableValue(bytes, index, (Integer) value);
            case "[Ljava.lang.Integer;":
                return IntegerArraySerializable.serializableValue(bytes, index, (Integer[]) value);
            case "java.math.BigDecimal":
                return BigDecimalSerializable.serializableValue(bytes, index, (BigDecimal) value);
            case "[Ljava.math.BigDecimal;":
                return BigDecimalArraySerializable.serializableValue(bytes, index, (BigDecimal[]) value);
            case "java.math.BigInteger":
                return BigIntegerSerializable.serializableValue(bytes, index, (BigInteger) value);
            case "[Ljava.math.BigInteger;":
                return BigIntegerArraySerializable.serializableValue(bytes, index, (BigInteger[]) value);
            case "java.lang.Byte":
                return ByteSerializable.serializableValue(bytes, index, (Byte) value);
            case "[Ljava.lang.Byte;":
                return ByteArraySerializable.serializableValue(bytes, index, (Byte[]) value);
            case "java.lang.Boolean":
                return BooleanSerializable.serializableValue(bytes, index, (Boolean) value);
            case "[Ljava.lang.Boolean;":
                return BooleanArraySerializable.serializableValue(bytes, index, (Boolean[]) value);
            case "java.lang.Character":
                return CharacterSerializable.serializableValue(bytes, index, (Character) value);
            case "[Ljava.lang.Character;":
                return CharacterArraySerializable.serializableValue(bytes, index, (Character[]) value);
            case "java.util.Date":
                return DateSerializable.serializableValue(bytes, index, (Date) value);
            case "[Ljava.util.Date;":
                return DateArraySerializable.serializableValue(bytes, index, (Date[]) value);
            case "java.lang.Double":
                return DoubleSerializable.serializableValue(bytes, index, (Double) value);
            case "[Ljava.lang.Double;":
                return DoubleArraySerializable.serializableValue(bytes, index, (Double[]) value);
            case "java.lang.Float":
                return FloatSerializable.serializableValue(bytes, index, (Float) value);
            case "[Ljava.lang.Float;":
                return FloatArraySerializable.serializableValue(bytes, index, (Float[]) value);
            case "java.lang.Long":
                return LongSerializable.serializableValue(bytes, index, (Long) value);
            case "[Ljava.lang.Long;":
                return LongArraySerializable.serializableValue(bytes, index, (Long[]) value);
            case "java.lang.Short":
                return ShortSerializable.serializableValue(bytes, index, (Short) value);
            case "[Ljava.lang.ShortSerializable;":
                return ShortArraySerializable.serializableValue(bytes, index, (Short[]) value);
            case "java.sql.Date":
                return SqlDateSerializable.serializableValue(bytes, index, (java.sql.Date) value);
            case "[Ljava.sql.Date;":
                return SqlDateArraySerializable.serializableValue(bytes, index, (java.sql.Date[]) value);
            case "java.lang.String":
                return StringSerializable.serializableValue(bytes, index, (String) value);
            case "[Ljava.lang.String;":
                return StringArraySerializable.serializableValue(bytes, index, (String[]) value);
            case "java.lang.StringBuffer":
                return StringBufferSerializable.serializableValue(bytes, index, (StringBuffer) value);
            case "[Ljava.lang.StringBuffer;":
                return StringBufferArraySerializable.serializableValue(bytes, index, (StringBuffer[]) value);
            case "java.lang.StringBuilder":
                return StringBuilderSerializable.serializableValue(bytes, index, (StringBuilder) value);
            case "[Ljava.lang.StringBuilder;":
                return StringBuilderArraySerializable.serializableValue(bytes, index, (StringBuilder[]) value);
            case "java.sql.Timestamp":
                return TimestampSerializable.serializableValue(bytes, index, (java.sql.Timestamp) value);
            case "[Ljava.sql.Timestamp;":
                return TimestampArraySerializable.serializableValue(bytes, index, (java.sql.Timestamp[]) value);
            default:
                if (value instanceof Collection) {
                    return CollectionSerializable.serializableValue(bytes, index, (Collection) value);
                } else if (value instanceof Map) {
                    return MapSerializable.serializableValue(bytes, index, (Map) value);
                } else if (value instanceof EntityService) {
                    return EntityNotDepthSerializable.serializableValue(bytes, index, (EntityService) value);
                }
                throw new SerializableException("An unsupported serialization type :" + className);
        }
    }

    /**
     * 值字节长度,非深度
     *
     * @param value 值
     * @author chengao chengao163postbox@163.com
     * @date 2021/10/20 18:35
     */
    public static int valueByteLengthNotDepth(Object value) {
        if (value == null) {
            return 0;
        }
        String className = value.getClass().getName();
        switch (className) {
            case "[I":
                return IntArraySerializable.valueByteLength((int[]) value);
            case "[D":
                return DouArraySerializable.valueByteLength((double[]) value);
            case "[B":
                return BytArraySerializable.valueByteLength((byte[]) value);
            case "[J":
                return LonArraySerializable.valueByteLength((long[]) value);
            case "[F":
                return FloArraySerializable.valueByteLength((float[]) value);
            case "[Z":
                return BoolArraySerializable.valueByteLength((boolean[]) value);
            case "[S":
                return ShoArraySerializable.valueByteLength((short[]) value);
            case "[C":
                return CharArraySerializable.valueByteLength((char[]) value);
            case "java.lang.Integer":
                return IntegerSerializable.valueByteLength((Integer) value);
            case "[Ljava.lang.Integer;":
                return IntegerArraySerializable.valueByteLength((Integer[]) value);
            case "java.math.BigDecimal":
                return BigDecimalSerializable.valueByteLength((BigDecimal) value);
            case "[Ljava.math.BigDecimal;":
                return BigDecimalArraySerializable.valueByteLength((BigDecimal[]) value);
            case "java.math.BigInteger":
                return BigIntegerSerializable.valueByteLength((BigInteger) value);
            case "[Ljava.math.BigInteger;":
                return BigIntegerArraySerializable.valueByteLength((BigInteger[]) value);
            case "java.lang.Byte":
                return ByteSerializable.valueByteLength((Byte) value);
            case "[Ljava.lang.Byte;":
                return ByteArraySerializable.valueByteLength((Byte[]) value);
            case "java.lang.Boolean":
                return BooleanSerializable.valueByteLength((Boolean) value);
            case "[Ljava.lang.Boolean;":
                return BooleanArraySerializable.valueByteLength((Boolean[]) value);
            case "java.lang.Character":
                return CharacterSerializable.valueByteLength((Character) value);
            case "[Ljava.lang.Character;":
                return CharacterArraySerializable.valueByteLength((Character[]) value);
            case "java.util.Date":
                return DateSerializable.valueByteLength((Date) value);
            case "[Ljava.util.Date;":
                return DateArraySerializable.valueByteLength((Date[]) value);
            case "java.lang.Double":
                return DoubleSerializable.valueByteLength((Double) value);
            case "[Ljava.lang.Double;":
                return DoubleArraySerializable.valueByteLength((Double[]) value);
            case "java.lang.Float":
                return FloatSerializable.valueByteLength((Float) value);
            case "[Ljava.lang.Float;":
                return FloatArraySerializable.valueByteLength((Float[]) value);
            case "java.lang.Long":
                return LongSerializable.valueByteLength((Long) value);
            case "[Ljava.lang.Long;":
                return LongArraySerializable.valueByteLength((Long[]) value);
            case "java.lang.Short":
                return ShortSerializable.valueByteLength((Short) value);
            case "[Ljava.lang.Short;":
                return ShortArraySerializable.valueByteLength((Short[]) value);
            case "java.sql.Date":
                return SqlDateSerializable.valueByteLength((java.sql.Date) value);
            case "[Ljava.sql.Date;":
                return SqlDateArraySerializable.valueByteLength((java.sql.Date[]) value);
            case "java.lang.String":
                return StringSerializable.valueByteLength((String) value);
            case "[Ljava.lang.String;":
                return StringArraySerializable.valueByteLength((String[]) value);
            case "java.lang.StringBuffer":
                return StringBufferSerializable.valueByteLength((StringBuffer) value);
            case "[Ljava.lang.StringBuffer;":
                return StringBufferArraySerializable.valueByteLength((StringBuffer[]) value);
            case "java.lang.StringBuilder":
                return StringBuilderSerializable.valueByteLength((StringBuilder) value);
            case "[Ljava.lang.StringBuilder;":
                return StringBuilderArraySerializable.valueByteLength((StringBuilder[]) value);
            case "java.sql.Timestamp":
                return TimestampSerializable.valueByteLength((java.sql.Timestamp) value);
            case "[Ljava.sql.Timestamp;":
                return TimestampArraySerializable.valueByteLength((java.sql.Timestamp[]) value);
            default:
                if (value instanceof Collection) {
                    return CollectionSerializable.valueByteLength((Collection) value);
                } else if (value instanceof Map) {
                    return MapSerializable.valueByteLength((Map) value);
                } else if (value instanceof EntityService) {
                    return EntityNotDepthSerializable.valueByteLength((EntityService) value);
                }
                throw new SerializableException("An unsupported serialization type :" + className);
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
    public static Object deserializationValue(ByteBuffer byteBuffer) {
        byte type = byteBuffer.get();
        if (type == 0) {
            return null;
        }
        byteBuffer.position(byteBuffer.position() - 1);
        switch (type) {
            case BigDecimalArraySerializable.TYPE:
                return BigDecimalArraySerializable.deserializationValue(byteBuffer);
            case BigDecimalSerializable.TYPE:
                return BigDecimalSerializable.deserializationValue(byteBuffer);
            case BigIntegerArraySerializable.TYPE:
                return BigIntegerArraySerializable.deserializationValue(byteBuffer);
            case BigIntegerSerializable.TYPE:
                return BigIntegerSerializable.deserializationValue(byteBuffer);
            case BoolArraySerializable.TYPE:
                return BoolArraySerializable.deserializationValue(byteBuffer);
            case BooleanArraySerializable.TYPE:
                return BooleanArraySerializable.deserializationValue(byteBuffer);
            case BooleanSerializable.TYPE:
                return BooleanSerializable.deserializationValue(byteBuffer);
            case BoolSerializable.TYPE:
                return BoolSerializable.deserializationValue(byteBuffer);
            case BytArraySerializable.TYPE:
                return BytArraySerializable.deserializationValue(byteBuffer);
            case ByteArraySerializable.TYPE:
                return ByteArraySerializable.deserializationValue(byteBuffer);
            case ByteSerializable.TYPE:
                return ByteSerializable.deserializationValue(byteBuffer);
            case BytSerializable.TYPE:
                return BytSerializable.deserializationValue(byteBuffer);
            case CharacterArraySerializable.TYPE:
                return CharacterArraySerializable.deserializationValue(byteBuffer);
            case CharacterSerializable.TYPE:
                return CharacterSerializable.deserializationValue(byteBuffer);
            case CharArraySerializable.TYPE:
                return CharArraySerializable.deserializationValue(byteBuffer);
            case CharSerializable.TYPE:
                return CharSerializable.deserializationValue(byteBuffer);
            case CollectionSerializable.TYPE:
                return CollectionSerializable.deserializationValue(byteBuffer);
            case DateArraySerializable.TYPE:
                return DateArraySerializable.deserializationValue(byteBuffer);
            case DateSerializable.TYPE:
                return DateSerializable.deserializationValue(byteBuffer);
            case DouArraySerializable.TYPE:
                return DouArraySerializable.deserializationValue(byteBuffer);
            case DoubleArraySerializable.TYPE:
                return DoubleArraySerializable.deserializationValue(byteBuffer);
            case DoubleSerializable.TYPE:
                return DoubleSerializable.deserializationValue(byteBuffer);
            case DouSerializable.TYPE:
                return DouSerializable.deserializationValue(byteBuffer);
            case FloatArraySerializable.TYPE:
                return FloatArraySerializable.deserializationValue(byteBuffer);
            case FloatSerializable.TYPE:
                return FloatSerializable.deserializationValue(byteBuffer);
            case FloSerializable.TYPE:
                return FloSerializable.deserializationValue(byteBuffer);
            case FloArraySerializable.TYPE:
                return FloArraySerializable.deserializationValue(byteBuffer);
            case IntArraySerializable.TYPE:
                return IntArraySerializable.deserializationValue(byteBuffer);
            case IntegerArraySerializable.TYPE:
                return IntegerArraySerializable.deserializationValue(byteBuffer);
            case IntegerSerializable.TYPE:
                return IntegerSerializable.deserializationValue(byteBuffer);
            case IntSerializable.TYPE:
                return IntSerializable.deserializationValue(byteBuffer);
            case LonArraySerializable.TYPE:
                return LonArraySerializable.deserializationValue(byteBuffer);
            case LongArraySerializable.TYPE:
                return LongArraySerializable.deserializationValue(byteBuffer);
            case LongSerializable.TYPE:
                return LongSerializable.deserializationValue(byteBuffer);
            case LonSerializable.TYPE:
                return LonSerializable.deserializationValue(byteBuffer);
            case ShoArraySerializable.TYPE:
                return ShoArraySerializable.deserializationValue(byteBuffer);
            case ShortArraySerializable.TYPE:
                return ShortArraySerializable.deserializationValue(byteBuffer);
            case ShortSerializable.TYPE:
                return ShortSerializable.deserializationValue(byteBuffer);
            case ShoSerializable.TYPE:
                return ShoSerializable.deserializationValue(byteBuffer);
            case SqlDateArraySerializable.TYPE:
                return SqlDateArraySerializable.deserializationValue(byteBuffer);
            case SqlDateSerializable.TYPE:
                return SqlDateSerializable.deserializationValue(byteBuffer);
            case StringArraySerializable.TYPE:
                return StringArraySerializable.deserializationValue(byteBuffer);
            case StringBufferArraySerializable.TYPE:
                return StringBufferArraySerializable.deserializationValue(byteBuffer);
            case StringBufferSerializable.TYPE:
                return StringBufferSerializable.deserializationValue(byteBuffer);
            case StringBuilderArraySerializable.TYPE:
                return StringBuilderArraySerializable.deserializationValue(byteBuffer);
            case StringBuilderSerializable.TYPE:
                return StringBuilderSerializable.deserializationValue(byteBuffer);
            case StringSerializable.TYPE:
                return StringSerializable.deserializationValue(byteBuffer);
            case TimestampArraySerializable.TYPE:
                return TimestampArraySerializable.deserializationValue(byteBuffer);
            case TimestampSerializable.TYPE:
                return TimestampSerializable.deserializationValue(byteBuffer);
            case MapSerializable.TYPE:
                return MapSerializable.deserializationValue(byteBuffer);
            case EntityNotDepthSerializable.TYPE:
                return EntityNotDepthSerializable.deserializationValue(byteBuffer);
            default:
                throw new SerializableException("An unsupported serialization type :" + type);
        }
    }

}
