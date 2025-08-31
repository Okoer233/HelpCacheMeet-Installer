package com.aeterhilrin.datapackinstaller.exception;

/**
 * 环境验证异常类
 * 当运行环境不符合要求时抛出
 * 
 * @author AeterHilrin
 */
public class EnvironmentValidationException extends Exception {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public EnvironmentValidationException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public EnvironmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}