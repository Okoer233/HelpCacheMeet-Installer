package com.aeterhilrin.datapackinstaller.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志条目数据模型类
 * 用于表示和格式化日志信息
 * 
 * @author AeterHilrin
 */
public class LogEntry {
    
    /** 时间戳 */
    private LocalDateTime timestamp;
    
    /** 日志级别 */
    private LogLevel level;
    
    /** 日志消息 */
    private String message;
    
    /** 组件名称 */
    private String component;
    
    /** 异常信息（可选） */
    private Throwable exception;
    
    /** 日志级别枚举 */
    public enum LogLevel {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR"),
        DEBUG("DEBUG");
        
        private final String displayName;
        
        LogLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param level 日志级别
     * @param component 组件名称
     * @param message 日志消息
     */
    public LogEntry(LogLevel level, String component, String message) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.component = component;
        this.message = message;
    }
    
    /**
     * 构造函数（包含异常信息）
     * 
     * @param level 日志级别
     * @param component 组件名称
     * @param message 日志消息
     * @param exception 异常信息
     */
    public LogEntry(LogLevel level, String component, String message, Throwable exception) {
        this(level, component, message);
        this.exception = exception;
    }
    
    /**
     * 格式化日志条目为字符串
     * 格式：[时间戳] [级别] [组件] 消息
     * 
     * @return 格式化后的日志字符串
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        
        // 添加时间戳
        sb.append("[").append(formatTimestamp()).append("]");
        
        // 添加日志级别
        sb.append(" [").append(formatLevel()).append("]");
        
        // 添加组件名称
        sb.append(" [").append(formatComponent()).append("]");
        
        // 添加消息
        sb.append(" ").append(message);
        
        // 如果有异常信息，添加异常详情
        if (exception != null) {
            sb.append(System.lineSeparator());
            sb.append("异常详情: ").append(exception.getClass().getSimpleName());
            sb.append(" - ").append(exception.getMessage());
            
            // 添加异常堆栈跟踪（简化版）
            StackTraceElement[] stackTrace = exception.getStackTrace();
            if (stackTrace.length > 0) {
                sb.append(System.lineSeparator());
                sb.append("位置: ").append(stackTrace[0].toString());
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化时间戳
     * 
     * @return 格式化后的时间戳字符串
     */
    private String formatTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    /**
     * 格式化日志级别
     * 确保所有级别都有固定的宽度
     * 
     * @return 格式化后的级别字符串
     */
    private String formatLevel() {
        return String.format("%-5s", level.getDisplayName());
    }
    
    /**
     * 格式化组件名称
     * 提取类名的简短形式
     * 
     * @return 格式化后的组件名称
     */
    private String formatComponent() {
        if (component == null || component.isEmpty()) {
            return "UNKNOWN";
        }
        
        // 如果是完整的类名，只返回类名部分
        int lastDotIndex = component.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < component.length() - 1) {
            return component.substring(lastDotIndex + 1);
        }
        
        return component;
    }
    
    /**
     * 获取简化的日志消息（用于控制台输出）
     * 
     * @return 简化的日志字符串
     */
    public String getSimpleFormat() {
        return String.format("[%s] %s: %s", 
                level.getDisplayName(), 
                formatComponent(), 
                message);
    }
    
    /**
     * 检查是否为错误级别的日志
     * 
     * @return 是否为错误级别
     */
    public boolean isError() {
        return level == LogLevel.ERROR;
    }
    
    /**
     * 检查是否为警告级别的日志
     * 
     * @return 是否为警告级别
     */
    public boolean isWarning() {
        return level == LogLevel.WARN;
    }
    
    /**
     * 检查是否包含异常信息
     * 
     * @return 是否包含异常
     */
    public boolean hasException() {
        return exception != null;
    }
    
    // Getter和Setter方法
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public LogLevel getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getComponent() {
        return component;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public void setException(Throwable exception) {
        this.exception = exception;
    }
    
    @Override
    public String toString() {
        return format();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LogEntry logEntry = (LogEntry) obj;
        
        if (!timestamp.equals(logEntry.timestamp)) return false;
        if (level != logEntry.level) return false;
        if (!message.equals(logEntry.message)) return false;
        return component != null ? component.equals(logEntry.component) : logEntry.component == null;
    }
    
    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + level.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + (component != null ? component.hashCode() : 0);
        return result;
    }
}