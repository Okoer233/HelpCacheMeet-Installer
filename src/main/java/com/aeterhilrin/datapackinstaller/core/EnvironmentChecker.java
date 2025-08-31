package com.aeterhilrin.datapackinstaller.core;

import com.aeterhilrin.datapackinstaller.exception.EnvironmentValidationException;
import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.utils.FileUtils;

import java.io.File;

/**
 * 环境检测器
 * 负责验证软件运行环境是否符合要求
 * 根据设计文档：软件应放在存档根目录，与datapacks文件夹和level.dat文件同级
 * 
 * @author AeterHilrin
 */
public class EnvironmentChecker {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 数据包文件夹名称 */
    private static final String DATAPACKS_FOLDER_NAME = "datapacks";
    
    /** 存档文件名称 */
    private static final String LEVEL_DAT_FILE_NAME = "level.dat";
    
    /** 日志文件夹名称 */
    private static final String LOG_FOLDER_NAME = "HelpCacheMeetLogs";
    
    /**
     * 执行完整的环境验证
     * 
     * @throws EnvironmentValidationException 环境验证失败
     */
    public void validateEnvironment() throws EnvironmentValidationException {
        logManager.logInfo("EnvironmentChecker", "开始环境检测...");
        
        try {
            // 检测同级目录中的datapacks文件夹
            if (!hasSiblingDatapacksFolder()) {
                throw new EnvironmentValidationException("同级目录中未找到datapacks文件夹");
            }
            
            // 检测同级目录中的level.dat文件
            if (!hasSiblingLevelDat()) {
                throw new EnvironmentValidationException("同级目录中未找到level.dat文件");
            }
            
            // 创建日志目录
            if (!createLogDirectory()) {
                throw new EnvironmentValidationException("无法创建日志目录");
            }
            
            logManager.logInfo("EnvironmentChecker", "环境检测通过");
            
        } catch (EnvironmentValidationException e) {
            logManager.logError("EnvironmentChecker", "环境检测失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logManager.logError("EnvironmentChecker", "环境检测过程中发生未知错误", e);
            throw new EnvironmentValidationException("环境检测过程中发生未知错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检测同级目录中是否存在datapacks文件夹
     * 
     * @return 是否存在datapacks文件夹
     */
    public boolean hasSiblingDatapacksFolder() {
        try {
            String currentDir = getCurrentDirectory();
            String datapacksPath = FileUtils.safeBuildPath(currentDir, DATAPACKS_FOLDER_NAME);
            
            if (datapacksPath == null) {
                logManager.logError("EnvironmentChecker", "构建datapacks文件夹路径失败");
                return false;
            }
            
            boolean exists = FileUtils.isDirectory(datapacksPath);
            
            if (exists) {
                logManager.logInfo("EnvironmentChecker", "检测到datapacks文件夹: " + datapacksPath);
            } else {
                logManager.logWarn("EnvironmentChecker", "未找到datapacks文件夹: " + datapacksPath);
            }
            
            return exists;
            
        } catch (Exception e) {
            logManager.logError("EnvironmentChecker", "检测datapacks文件夹时发生错误", e);
            return false;
        }
    }
    
    /**
     * 检测同级目录中是否存在level.dat文件
     * 
     * @return 是否存在level.dat文件
     */
    public boolean hasSiblingLevelDat() {
        try {
            String currentDir = getCurrentDirectory();
            String levelDatPath = FileUtils.safeBuildPath(currentDir, LEVEL_DAT_FILE_NAME);
            
            if (levelDatPath == null) {
                logManager.logError("EnvironmentChecker", "构建level.dat文件路径失败");
                return false;
            }
            
            boolean exists = FileUtils.isFile(levelDatPath);
            
            if (exists) {
                logManager.logInfo("EnvironmentChecker", "检测到level.dat文件: " + levelDatPath);
            } else {
                logManager.logWarn("EnvironmentChecker", "未找到level.dat文件: " + levelDatPath);
            }
            
            return exists;
            
        } catch (Exception e) {
            logManager.logError("EnvironmentChecker", "检测level.dat文件时发生错误", e);
            return false;
        }
    }
    
    /**
     * 在同级目录中创建HelpCacheMeetLogs日志目录
     * 
     * @return 创建是否成功
     */
    public boolean createLogDirectory() {
        try {
            String currentDir = getCurrentDirectory();
            String logDirPath = FileUtils.safeBuildPath(currentDir, LOG_FOLDER_NAME);
            
            if (logDirPath == null) {
                logManager.logError("EnvironmentChecker", "构建日志目录路径失败");
                return false;
            }
            
            if (FileUtils.isDirectory(logDirPath)) {
                logManager.logInfo("EnvironmentChecker", "日志目录已存在: " + logDirPath);
                return true;
            }
            
            boolean created = FileUtils.createDirectories(logDirPath);
            
            if (created) {
                logManager.logInfo("EnvironmentChecker", "成功创建日志目录: " + logDirPath);
            } else {
                logManager.logError("EnvironmentChecker", "创建日志目录失败: " + logDirPath);
            }
            
            return created;
            
        } catch (Exception e) {
            logManager.logError("EnvironmentChecker", "创建日志目录时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取当前工作目录
     * 
     * @return 当前工作目录路径
     */
    private String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * 获取datapacks文件夹的完整路径
     * 
     * @return datapacks文件夹路径，如果不存在则返回null
     */
    public String getDatapacksFolder() {
        String currentDir = getCurrentDirectory();
        String datapacksPath = FileUtils.safeBuildPath(currentDir, DATAPACKS_FOLDER_NAME);
        
        if (datapacksPath != null && FileUtils.isDirectory(datapacksPath)) {
            return datapacksPath;
        }
        
        return null;
    }
    
    /**
     * 获取level.dat文件的完整路径
     * 
     * @return level.dat文件路径，如果不存在则返回null
     */
    public String getLevelDatFile() {
        String currentDir = getCurrentDirectory();
        String levelDatPath = FileUtils.safeBuildPath(currentDir, LEVEL_DAT_FILE_NAME);
        
        if (levelDatPath != null && FileUtils.isFile(levelDatPath)) {
            return levelDatPath;
        }
        
        return null;
    }
    
    /**
     * 获取日志目录的完整路径
     * 
     * @return 日志目录路径
     */
    public String getLogDirectory() {
        String currentDir = getCurrentDirectory();
        return FileUtils.safeBuildPath(currentDir, LOG_FOLDER_NAME);
    }
    
    /**
     * 检查文件系统权限
     * 验证是否有足够的权限进行文件操作
     * 
     * @return 权限检查结果
     */
    public boolean checkFileSystemPermissions() {
        try {
            String currentDir = getCurrentDirectory();
            File currentDirFile = new File(currentDir);
            
            // 检查读取权限
            if (!currentDirFile.canRead()) {
                logManager.logError("EnvironmentChecker", "当前目录没有读取权限: " + currentDir);
                return false;
            }
            
            // 检查写入权限
            if (!currentDirFile.canWrite()) {
                logManager.logError("EnvironmentChecker", "当前目录没有写入权限: " + currentDir);
                return false;
            }
            
            // 检查datapacks文件夹权限
            String datapacksPath = getDatapacksFolder();
            if (datapacksPath != null) {
                File datapacksDir = new File(datapacksPath);
                if (!datapacksDir.canRead() || !datapacksDir.canWrite()) {
                    logManager.logError("EnvironmentChecker", "datapacks文件夹权限不足: " + datapacksPath);
                    return false;
                }
            }
            
            // 检查level.dat文件权限
            String levelDatPath = getLevelDatFile();
            if (levelDatPath != null) {
                File levelDatFile = new File(levelDatPath);
                if (!levelDatFile.canRead() || !levelDatFile.canWrite()) {
                    logManager.logError("EnvironmentChecker", "level.dat文件权限不足: " + levelDatPath);
                    return false;
                }
            }
            
            logManager.logInfo("EnvironmentChecker", "文件系统权限检查通过");
            return true;
            
        } catch (Exception e) {
            logManager.logError("EnvironmentChecker", "检查文件系统权限时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取环境信息摘要
     * 
     * @return 环境信息字符串
     */
    public String getEnvironmentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("环境信息摘要:\n");
        summary.append("- 当前目录: ").append(getCurrentDirectory()).append("\n");
        summary.append("- datapacks文件夹: ").append(hasSiblingDatapacksFolder() ? "存在" : "不存在").append("\n");
        summary.append("- level.dat文件: ").append(hasSiblingLevelDat() ? "存在" : "不存在").append("\n");
        summary.append("- 日志目录: ").append(getLogDirectory()).append("\n");
        summary.append("- 文件系统权限: ").append(checkFileSystemPermissions() ? "正常" : "异常");
        
        return summary.toString();
    }
}