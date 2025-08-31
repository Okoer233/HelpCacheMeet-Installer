package com.aeterhilrin.datapackinstaller.exception;

/**
 * 依赖解析异常类
 * 当数据包依赖关系解析失败时抛出
 * 
 * @author AeterHilrin
 */
public class DependencyResolutionException extends Exception {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public DependencyResolutionException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}