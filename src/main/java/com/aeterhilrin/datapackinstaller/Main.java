package com.aeterhilrin.datapackinstaller;

import com.aeterhilrin.datapackinstaller.core.MainController;
import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.ui.UIManager;

/**
 * HelpCacheMeetDatapackInstaller
 * 
 * 项目名称: HelpCacheMeetDatapackInstaller
 * 作者: AeterHilrin
 * 开发语言: Java 8
 * 许可证: MIT协议
 * 功能描述: 自动化Minecraft数据包安装和管理工具，负责NBT文件修改、依赖关系排序和数据包启用管理
 * 
 * @author AeterHilrin
 */
public class Main {
    
    /** 应用程序名称 */
    private static final String APPLICATION_NAME = "HelpCacheMeetDatapackInstaller";
    
    /** 应用程序版本 */
    private static final String APPLICATION_VERSION = "1.0.0";
    
    /** 作者信息 */
    private static final String AUTHOR = "AeterHilrin";
    
    /** 日志管理器 */
    private static LogManager logManager;
    
    /** UI管理器 */
    private static UIManager uiManager;
    
    /**
     * 主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 初始化应用程序
            initialize(args);
            
            // 打印应用程序信息
            printApplicationInfo();
            
            // 创建并运行主控制器
            MainController mainController = new MainController();
            mainController.run();
            
        } catch (Exception e) {
            handleFatalError("应用程序启动失败", e);
        }
    }
    
    /**
     * 初始化应用程序
     * 
     * @param args 命令行参数
     */
    private static void initialize(String[] args) {
        try {
            // 解析命令行参数
            parseCommandLineArguments(args);
            
            // 初始化日志系统
            initializeLogging();
            
            // 初始化UI系统
            initializeUI();
            
            logManager.logInfo("Main", "应用程序初始化完成");
            
        } catch (Exception e) {
            System.err.println("初始化失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 解析命令行参数
     * 
     * @param args 命令行参数
     */
    private static void parseCommandLineArguments(String[] args) {
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg.toLowerCase()) {
                    case "--version":
                    case "-v":
                        printVersionInfo();
                        System.exit(0);
                        break;
                        
                    case "--help":
                    case "-h":
                        printHelpInfo();
                        System.exit(0);
                        break;
                        
                    case "--debug":
                    case "-d":
                        // 启用调试模式（这里可以设置调试标志）
                        System.setProperty("debug.mode", "true");
                        break;
                        
                    default:
                        System.err.println("未知参数: " + arg);
                        printHelpInfo();
                        System.exit(1);
                        break;
                }
            }
        }
    }
    
    /**
     * 初始化日志系统
     */
    private static void initializeLogging() {
        try {
            logManager = LogManager.getInstance();
            logManager.initialize();
            
        } catch (Exception e) {
            System.err.println("日志系统初始化失败: " + e.getMessage());
            throw new RuntimeException("无法初始化日志系统", e);
        }
    }
    
    /**
     * 初始化UI系统
     */
    private static void initializeUI() {
        try {
            uiManager = UIManager.getInstance();
            uiManager.initialize();
            
        } catch (Exception e) {
            logManager.logError("Main", "UI系统初始化失败", e);
            throw new RuntimeException("无法初始化UI系统", e);
        }
    }
    
    /**
     * 打印应用程序信息
     */
    private static void printApplicationInfo() {
        logManager.logInfo("Main", "=========================================");
        logManager.logInfo("Main", "应用程序: " + APPLICATION_NAME);
        logManager.logInfo("Main", "版本: " + APPLICATION_VERSION);
        logManager.logInfo("Main", "作者: " + AUTHOR);
        logManager.logInfo("Main", "Java版本: " + System.getProperty("java.version"));
        logManager.logInfo("Main", "操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        logManager.logInfo("Main", "工作目录: " + System.getProperty("user.dir"));
        logManager.logInfo("Main", "=========================================");
    }
    
    /**
     * 打印版本信息
     */
    private static void printVersionInfo() {
        System.out.println(APPLICATION_NAME + " " + APPLICATION_VERSION);
        System.out.println("作者: " + AUTHOR);
        System.out.println("基于MIT协议开源");
    }
    
    /**
     * 打印帮助信息
     */
    private static void printHelpInfo() {
        System.out.println("用法: java -jar " + APPLICATION_NAME + ".jar [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -h, --help     显示此帮助信息");
        System.out.println("  -v, --version  显示版本信息");
        System.out.println("  -d, --debug    启用调试模式");
        System.out.println();
        System.out.println("说明:");
        System.out.println("  本工具用于自动安装和管理Minecraft数据包。");
        System.out.println("  请将此程序放置在Minecraft存档根目录中运行。");
        System.out.println("  程序会自动检测datapacks文件夹中的数据包，");
        System.out.println("  解析依赖关系，并更新level.dat文件。");
        System.out.println();
        System.out.println("环境要求:");
        System.out.println("  • 与datapacks文件夹在同一目录");
        System.out.println("  • 与level.dat文件在同一目录");
        System.out.println("  • 具有文件读写权限");
        System.out.println();
        System.out.println("作者: " + AUTHOR);
        System.out.println("许可证: MIT");
    }
    
    /**
     * 处理致命错误
     * 
     * @param message 错误消息
     * @param exception 异常对象
     */
    private static void handleFatalError(String message, Exception exception) {
        // 首先尝试记录到日志
        if (logManager != null) {
            logManager.logError("Main", message, exception);
        } else {
            System.err.println("致命错误: " + message);
            if (exception != null) {
                exception.printStackTrace();
            }
        }
        
        // 尝试显示错误对话框
        if (uiManager != null) {
            try {
                String errorDetails = exception != null ? exception.getMessage() : "未知错误";
                uiManager.showErrorMessage(message + "\n\n错误详情: " + errorDetails);
            } catch (Exception e) {
                System.err.println("显示错误对话框失败: " + e.getMessage());
            }
        } else {
            System.err.println("无法显示图形界面错误信息");
        }
        
        // 退出程序
        System.exit(1);
    }
    
    /**
     * JVM关闭钩子
     * 确保程序退出时进行必要的清理
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (logManager != null) {
                    logManager.logInfo("Main", "应用程序正在关闭...");
                    logManager.shutdown();
                }
            } catch (Exception e) {
                System.err.println("关闭清理时发生错误: " + e.getMessage());
            }
        }));
    }
    
    /**
     * 获取应用程序名称
     * 
     * @return 应用程序名称
     */
    public static String getApplicationName() {
        return APPLICATION_NAME;
    }
    
    /**
     * 获取应用程序版本
     * 
     * @return 应用程序版本
     */
    public static String getApplicationVersion() {
        return APPLICATION_VERSION;
    }
    
    /**
     * 获取作者信息
     * 
     * @return 作者信息
     */
    public static String getAuthor() {
        return AUTHOR;
    }
    
    /**
     * 检查是否为调试模式
     * 
     * @return 是否为调试模式
     */
    public static boolean isDebugMode() {
        return "true".equals(System.getProperty("debug.mode"));
    }
}