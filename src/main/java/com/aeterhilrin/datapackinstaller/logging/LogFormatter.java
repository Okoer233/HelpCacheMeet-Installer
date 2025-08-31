package com.aeterhilrin.datapackinstaller.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 自定义日志格式化器
 * 用于统一格式化应用程序的日志输出
 * 
 * @author AeterHilrin
 */
public class LogFormatter extends Formatter {
    
    /** 时间戳格式化器 */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 换行符 */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    /**
     * 格式化日志记录
     * 格式：[时间戳] [级别] [类名] 消息
     * 
     * @param record 日志记录
     * @return 格式化后的日志字符串
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        
        // 添加时间戳
        sb.append("[").append(formatTimestamp(record.getMillis())).append("]");
        
        // 添加日志级别
        sb.append(" [").append(formatLevel(record.getLevel().getName())).append("]");
        
        // 添加类名
        sb.append(" [").append(formatClassName(record.getSourceClassName())).append("]");
        
        // 添加消息
        sb.append(" ").append(formatMessage(record));
        
        // 添加换行符
        sb.append(LINE_SEPARATOR);
        
        // 如果有异常信息，添加异常详情
        if (record.getThrown() != null) {
            sb.append(formatException(record.getThrown()));
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化时间戳
     * 
     * @param millis 毫秒时间戳
     * @return 格式化后的时间字符串
     */
    private String formatTimestamp(long millis) {
        try {
            // 使用简单的时间转换，避免复杂的时区操作
            java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            return TIMESTAMP_FORMATTER.format(dateTime);
        } catch (Exception e) {
            // 如果时间格式化失败，返回原始毫秒值
            return String.valueOf(millis);
        }
    }
    
    /**
     * 格式化日志级别
     * 确保所有级别都有固定的宽度
     * 
     * @param levelName 级别名称
     * @return 格式化后的级别字符串
     */
    private String formatLevel(String levelName) {
        return String.format("%-7s", levelName);
    }
    
    /**
     * 格式化类名
     * 提取类名的简短形式
     * 
     * @param className 完整类名
     * @return 格式化后的类名
     */
    private String formatClassName(String className) {
        if (className == null || className.isEmpty()) {
            return "UNKNOWN";
        }
        
        // 只返回类名部分，不包含包名
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < className.length() - 1) {
            return className.substring(lastDotIndex + 1);
        }
        
        return className;
    }
    
    /**
     * 格式化日志消息
     * 处理参数化消息
     * 
     * @param record 日志记录
     * @return 格式化后的消息
     */
    public String formatMessage(LogRecord record) {
        String message = record.getMessage();
        Object[] parameters = record.getParameters();
        
        if (parameters != null && parameters.length > 0) {
            try {
                // 简单的参数替换，支持 {0}, {1} 等占位符
                for (int i = 0; i < parameters.length; i++) {
                    String placeholder = "{" + i + "}";
                    if (message.contains(placeholder)) {
                        message = message.replace(placeholder, String.valueOf(parameters[i]));
                    }
                }
            } catch (Exception e) {
                // 如果参数替换失败，返回原始消息
                return record.getMessage();
            }
        }
        
        return message;
    }
    
    /**
     * 格式化异常信息
     * 
     * @param throwable 异常对象
     * @return 格式化后的异常字符串
     */
    private String formatException(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        
        // 添加异常类型和消息
        sb.append("异常: ").append(throwable.getClass().getSimpleName());
        if (throwable.getMessage() != null) {
            sb.append(" - ").append(throwable.getMessage());
        }
        sb.append(LINE_SEPARATOR);
        
        // 添加关键的堆栈跟踪信息（限制数量避免日志过长）
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int maxLines = Math.min(5, stackTrace.length); // 最多显示5行堆栈信息
        
        for (int i = 0; i < maxLines; i++) {
            sb.append("    at ").append(stackTrace[i].toString()).append(LINE_SEPARATOR);
        }
        
        if (stackTrace.length > maxLines) {
            sb.append("    ... 还有 ").append(stackTrace.length - maxLines).append(" 行")
              .append(LINE_SEPARATOR);
        }
        
        // 如果有原因异常，递归格式化
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            sb.append("原因: ");
            sb.append(formatException(throwable.getCause()));
        }
        
        return sb.toString();
    }
}