package com.aard.processor.exception;

/**
 * 序列化异常类
 *
 * @author chengao
 */
public class ClassInitException extends RuntimeException {

    public ClassInitException(String msg) {
        super(msg);
    }

    public ClassInitException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

