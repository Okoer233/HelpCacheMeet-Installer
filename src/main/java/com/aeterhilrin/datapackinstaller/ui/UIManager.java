package com.aeterhilrin.datapackinstaller.ui;

import com.aeterhilrin.datapackinstaller.logging.LogManager;

import javax.swing.*;
import java.awt.*;

/**
 * UI管理器
 * 负责管理应用程序的用户界面
 * 
 * @author AeterHilrin
 */
public class UIManager {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 单例实例 */
    private static UIManager instance;
    
    /** 是否已初始化 */
    private boolean initialized = false;
    
    /**
     * 私有构造函数（单例模式）
     */
    private UIManager() {
        // 私有构造函数
    }
    
    /**
     * 获取UI管理器实例
     * 
     * @return UI管理器实例
     */
    public static synchronized UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager();
        }
        return instance;
    }
    
    /**
     * 初始化UI系统
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // 设置系统外观
            setSystemLookAndFeel();
            
            // 设置UI字体
            setupUIFont();
            
            // 设置UI属性
            setupUIProperties();
            
            initialized = true;
            logManager.logInfo("UIManager", "UI系统初始化完成");
            
        } catch (Exception e) {
            logManager.logError("UIManager", "UI系统初始化失败", e);
        }
    }
    
    /**
     * 设置系统外观
     */
    private void setSystemLookAndFeel() {
        try {
            // 尝试使用系统默认外观
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            logManager.logDebug("UIManager", "已设置系统外观: " + 
                    javax.swing.UIManager.getLookAndFeel().getName());
            
        } catch (Exception e) {
            logManager.logWarn("UIManager", "设置系统外观失败，使用默认外观", e);
            
            try {
                // 如果系统外观失败，使用Nimbus外观
                for (javax.swing.UIManager.LookAndFeelInfo info : 
                     javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        logManager.logDebug("UIManager", "已设置Nimbus外观");
                        break;
                    }
                }
            } catch (Exception e2) {
                logManager.logWarn("UIManager", "设置Nimbus外观也失败，使用默认外观", e2);
            }
        }
    }
    
    /**
     * 设置UI字体
     */
    private void setupUIFont() {
        try {
            // 获取系统默认字体
            Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
            
            // 设置各种UI组件的字体
            java.util.Enumeration<Object> keys = javax.swing.UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = javax.swing.UIManager.get(key);
                if (value instanceof Font) {
                    javax.swing.UIManager.put(key, defaultFont);
                }
            }
            
            logManager.logDebug("UIManager", "UI字体设置完成");
            
        } catch (Exception e) {
            logManager.logWarn("UIManager", "设置UI字体失败", e);
        }
    }
    
    /**
     * 设置UI属性
     */
    private void setupUIProperties() {
        try {
            // 设置工具提示字体
            javax.swing.UIManager.put("ToolTip.font", new Font(Font.DIALOG, Font.PLAIN, 11));
            
            // 设置消息对话框属性
            javax.swing.UIManager.put("OptionPane.messageFont", new Font(Font.DIALOG, Font.PLAIN, 12));
            javax.swing.UIManager.put("OptionPane.buttonFont", new Font(Font.DIALOG, Font.PLAIN, 11));
            
            // 设置按钮属性
            javax.swing.UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
            
            logManager.logDebug("UIManager", "UI属性设置完成");
            
        } catch (Exception e) {
            logManager.logWarn("UIManager", "设置UI属性失败", e);
        }
    }
    
    /**
     * 显示协议确认对话框
     * 
     * @return 用户是否同意协议
     */
    public boolean showLicenseDialog() {
        ensureInitialized();
        return LicenseAgreementDialog.showLicenseDialog();
    }
    
    /**
     * 显示信息消息
     * 
     * @param message 消息内容
     */
    public void showInfoMessage(String message) {
        ensureInitialized();
        MessageDialog.showInfo(message);
    }
    
    /**
     * 显示成功消息
     * 
     * @param message 消息内容
     */
    public void showSuccessMessage(String message) {
        ensureInitialized();
        MessageDialog.showSuccess(message);
    }
    
    /**
     * 显示警告消息
     * 
     * @param message 消息内容
     */
    public void showWarningMessage(String message) {
        ensureInitialized();
        MessageDialog.showWarning(message);
    }
    
    /**
     * 显示错误消息
     * 
     * @param message 消息内容
     */
    public void showErrorMessage(String message) {
        ensureInitialized();
        MessageDialog.showError(message);
    }
    
    /**
     * 退出应用程序
     * 
     * @param exitCode 退出代码
     */
    public void exitApplication(int exitCode) {
        logManager.logInfo("UIManager", "应用程序退出，退出代码: " + exitCode);
        
        // 执行清理工作
        cleanup();
        
        // 退出程序
        System.exit(exitCode);
    }
    
    /**
     * 正常退出应用程序
     */
    public void exitApplication() {
        exitApplication(0);
    }
    
    /**
     * 因错误退出应用程序
     */
    public void exitWithError() {
        exitApplication(1);
    }
    
    /**
     * 确保UI系统已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        try {
            // 关闭所有窗口
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                window.dispose();
            }
            
            logManager.logDebug("UIManager", "UI资源清理完成");
            
        } catch (Exception e) {
            logManager.logWarn("UIManager", "清理UI资源时发生错误", e);
        }
    }
    
    /**
     * 检查是否在事件调度线程中
     * 
     * @return 是否在EDT中
     */
    public boolean isEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread();
    }
    
    /**
     * 在事件调度线程中执行代码
     * 
     * @param runnable 要执行的代码
     */
    public void invokeAndWait(Runnable runnable) {
        if (isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                logManager.logError("UIManager", "在EDT中执行代码失败", e);
            }
        }
    }
    
    /**
     * 在事件调度线程中稍后执行代码
     * 
     * @param runnable 要执行的代码
     */
    public void invokeLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * 显示确认对话框
     * 
     * @param message 确认消息
     * @param title 对话框标题
     * @return 用户是否确认
     */
    public boolean showConfirmDialog(String message, String title) {
        ensureInitialized();
        
        int result = JOptionPane.showConfirmDialog(
                null, 
                message, 
                title, 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * 显示确认对话框（默认标题）
     * 
     * @param message 确认消息
     * @return 用户是否确认
     */
    public boolean showConfirmDialog(String message) {
        return showConfirmDialog(message, "确认");
    }
    
    /**
     * 获取屏幕中心位置
     * 
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 中心位置坐标
     */
    public Point getScreenCenterLocation(int width, int height) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        return new Point(x, y);
    }
    
    /**
     * 检查UI系统是否已初始化
     * 
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取系统外观信息
     * 
     * @return 外观信息字符串
     */
    public String getLookAndFeelInfo() {
        LookAndFeel laf = javax.swing.UIManager.getLookAndFeel();
        if (laf != null) {
            return String.format("当前外观: %s (%s)", laf.getName(), laf.getClass().getName());
        } else {
            return "无法获取外观信息";
        }
    }
}