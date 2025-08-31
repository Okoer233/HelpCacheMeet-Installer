package com.aeterhilrin.datapackinstaller.exception;

/**
 * 循环依赖异常类
 * 当检测到数据包之间存在循环依赖时抛出
 * 
 * @author AeterHilrin
 */
public class CyclicDependencyException extends Exception {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public CyclicDependencyException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public CyclicDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}