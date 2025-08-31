package com.aeterhilrin.datapackinstaller.logging;

import com.aeterhilrin.datapackinstaller.model.LogEntry;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * 日志管理器
 * 负责配置和管理应用程序的日志记录
 * 
 * @author AeterHilrin
 */
public class LogManager {
    
    /** 单例实例 */
    private static LogManager instance;
    
    /** 应用程序日志记录器 */
    private Logger applicationLogger;
    
    /** 错误日志记录器 */
    private Logger errorLogger;
    
    /** 是否已初始化 */
    private boolean initialized = false;
    
    /** 日志目录路径 */
    private String logDirectoryPath;
    
    /**
     * 私有构造函数（单例模式）
     */
    private LogManager() {
        // 私有构造函数
    }
    
    /**
     * 获取日志管理器实例
     * 
     * @return 日志管理器实例
     */
    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    
    /**
     * 初始化日志系统
     * 在应用程序根目录下创建HelpCacheMeetLogs文件夹
     * 
     * @throws IOException 初始化失败
     */
    public void initialize() throws IOException {
        if (initialized) {
            return;
        }
        
        // 确定日志目录路径
        File currentDir = new File(System.getProperty("user.dir"));
        File logDir = new File(currentDir, "HelpCacheMeetLogs");
        this.logDirectoryPath = logDir.getAbsolutePath();
        
        // 创建日志目录
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new IOException("无法创建日志目录: " + logDir.getAbsolutePath());
        }
        
        // 设置全局日志级别
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        
        // 移除默认的控制台处理器（避免重复输出）
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        
        // 初始化应用程序日志记录器
        setupApplicationLogger(logDir);
        
        // 初始化错误日志记录器
        setupErrorLogger(logDir);
        
        // 添加控制台处理器（用于实时查看重要信息）
        setupConsoleLogger();
        
        initialized = true;
        
        logInfo("LogManager", "日志系统初始化完成，日志目录: " + logDirectoryPath);
    }
    
    /**
     * 设置应用程序日志记录器
     * 
     * @param logDir 日志目录
     * @throws IOException 设置失败
     */
    private void setupApplicationLogger(File logDir) throws IOException {
        applicationLogger = Logger.getLogger("Application");
        applicationLogger.setLevel(Level.ALL);
        applicationLogger.setUseParentHandlers(false);
        
        // 创建文件处理器
        File applicationLogFile = new File(logDir, "application.log");
        FileHandler fileHandler = new FileHandler(applicationLogFile.getAbsolutePath(), true);
        fileHandler.setFormatter(new LogFormatter());
        fileHandler.setLevel(Level.ALL);
        
        applicationLogger.addHandler(fileHandler);
    }
    
    /**
     * 设置错误日志记录器
     * 
     * @param logDir 日志目录
     * @throws IOException 设置失败
     */
    private void setupErrorLogger(File logDir) throws IOException {
        errorLogger = Logger.getLogger("Error");
        errorLogger.setLevel(Level.WARNING);
        errorLogger.setUseParentHandlers(false);
        
        // 创建错误日志文件处理器
        File errorLogFile = new File(logDir, "error.log");
        FileHandler errorFileHandler = new FileHandler(errorLogFile.getAbsolutePath(), true);
        errorFileHandler.setFormatter(new LogFormatter());
        errorFileHandler.setLevel(Level.WARNING);
        
        errorLogger.addHandler(errorFileHandler);
    }
    
    /**
     * 设置控制台日志记录器
     * 只显示WARNING级别以上的消息
     */
    private void setupConsoleLogger() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n",
                        record.getLevel().getName(),
                        record.getSourceClassName() != null ? 
                                getSimpleClassName(record.getSourceClassName()) : "UNKNOWN",
                        record.getMessage());
            }
        });
        
        // 添加到根日志记录器
        Logger.getLogger("").addHandler(consoleHandler);
    }
    
    /**
     * 获取简单类名
     * 
     * @param className 完整类名
     * @return 简单类名
     */
    private String getSimpleClassName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < className.length() - 1) {
            return className.substring(lastDotIndex + 1);
        }
        return className;
    }
    
    /**
     * 记录信息级别日志
     * 
     * @param component 组件名称
     * @param message 日志消息
     */
    public void logInfo(String component, String message) {
        if (!initialized) {
            System.out.println("[INFO] " + component + ": " + message);
            return;
        }
        
        LogEntry entry = new LogEntry(LogEntry.LogLevel.INFO, component, message);
        applicationLogger.info(entry.format());
    }
    
    /**
     * 记录警告级别日志
     * 
     * @param component 组件名称
     * @param message 日志消息
     */
    public void logWarn(String component, String message) {
        if (!initialized) {
            System.out.println("[WARN] " + component + ": " + message);
            return;
        }
        
        LogEntry entry = new LogEntry(LogEntry.LogLevel.WARN, component, message);
        applicationLogger.warning(entry.format());
        errorLogger.warning(entry.format());
    }
    
    /**
     * 记录警告级别日志（包含异常信息）
     * 
     * @param component 组件名称
     * @param message 日志消息
     * @param exception 异常对象
     */
    public void logWarn(String component, String message, Throwable exception) {
        if (!initialized) {
            System.out.println("[WARN] " + component + ": " + message);
            if (exception != null) {
                exception.printStackTrace();
            }
            return;
        }
        
        LogEntry entry = new LogEntry(LogEntry.LogLevel.WARN, component, message, exception);
        applicationLogger.warning(entry.format());
        errorLogger.warning(entry.format());
    }
    
    /**
     * 记录错误级别日志
     * 
     * @param component 组件名称
     * @param message 日志消息
     */
    public void logError(String component, String message) {
        logError(component, message, null);
    }
    
    /**
     * 记录错误级别日志（包含异常信息）
     * 
     * @param component 组件名称
     * @param message 日志消息
     * @param exception 异常对象
     */
    public void logError(String component, String message, Throwable exception) {
        if (!initialized) {
            System.err.println("[ERROR] " + component + ": " + message);
            if (exception != null) {
                exception.printStackTrace();
            }
            return;
        }
        
        LogEntry entry = new LogEntry(LogEntry.LogLevel.ERROR, component, message, exception);
        applicationLogger.severe(entry.format());
        errorLogger.severe(entry.format());
    }
    
    /**
     * 记录调试级别日志
     * 
     * @param component 组件名称
     * @param message 日志消息
     */
    public void logDebug(String component, String message) {
        if (!initialized) {
            return; // 调试信息在未初始化时不输出
        }
        
        LogEntry entry = new LogEntry(LogEntry.LogLevel.DEBUG, component, message);
        applicationLogger.fine(entry.format());
    }
    
    /**
     * 获取指定组件的日志记录器
     * 
     * @param componentClass 组件类
     * @return 日志记录器
     */
    public Logger getLogger(Class<?> componentClass) {
        if (!initialized) {
            try {
                initialize();
            } catch (IOException e) {
                System.err.println("初始化日志系统失败: " + e.getMessage());
                return Logger.getLogger(componentClass.getName());
            }
        }
        
        Logger logger = Logger.getLogger(componentClass.getName());
        logger.setLevel(Level.ALL);
        return logger;
    }
    
    /**
     * 关闭日志系统
     * 释放所有文件处理器
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        logInfo("LogManager", "正在关闭日志系统...");
        
        // 关闭应用程序日志记录器的处理器
        if (applicationLogger != null) {
            Handler[] handlers = applicationLogger.getHandlers();
            for (Handler handler : handlers) {
                handler.close();
                applicationLogger.removeHandler(handler);
            }
        }
        
        // 关闭错误日志记录器的处理器
        if (errorLogger != null) {
            Handler[] handlers = errorLogger.getHandlers();
            for (Handler handler : handlers) {
                handler.close();
                errorLogger.removeHandler(handler);
            }
        }
        
        initialized = false;
    }
    
    /**
     * 检查日志系统是否已初始化
     * 
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取日志目录路径
     * 
     * @return 日志目录路径
     */
    public String getLogDirectoryPath() {
        return logDirectoryPath;
    }
}