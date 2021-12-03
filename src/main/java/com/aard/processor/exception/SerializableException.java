package com.aard.processor.exception;

/**
 * 序列化异常类
 *
 * @author chengao
 */
public class SerializableException extends RuntimeException {

    public SerializableException(String msg) {
        super(msg);
    }

    public SerializableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

