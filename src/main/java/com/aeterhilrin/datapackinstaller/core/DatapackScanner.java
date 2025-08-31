package com.aeterhilrin.datapackinstaller.core;

import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;
import com.aeterhilrin.datapackinstaller.utils.FileUtils;
import com.aeterhilrin.datapackinstaller.utils.YamlConfigParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据包扫描器
 * 负责扫描datapacks文件夹中的所有数据包并解析其配置信息
 * 
 * @author AeterHilrin
 */
public class DatapackScanner {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 支持的ZIP文件扩展名 */
    private static final String ZIP_EXTENSION = "zip";
    
    /** 当前安装器的文件名模式（避免扫描自身） */
    private static final String INSTALLER_FILE_PATTERN = "HelpCacheMeetDatapackInstaller";
    
    /**
     * 扫描指定目录中的所有有效数据包
     * 
     * @param datapacksPath datapacks文件夹路径
     * @return 有效数据包信息列表
     */
    public List<DatapackInfo> scanDatapacks(String datapacksPath) {
        List<DatapackInfo> datapacks = new ArrayList<>();
        
        if (!FileUtils.isDirectory(datapacksPath)) {
            logManager.logError("DatapackScanner", "指定路径不是有效目录: " + datapacksPath);
            return datapacks;
        }
        
        logManager.logInfo("DatapackScanner", "开始扫描数据包目录: " + datapacksPath);
        
        try {
            List<String> entries = FileUtils.listDirectory(datapacksPath);
            
            for (String entryPath : entries) {
                DatapackInfo datapackInfo = processEntry(entryPath);
                if (datapackInfo != null) {
                    datapacks.add(datapackInfo);
                }
            }
            
            logManager.logInfo("DatapackScanner", 
                    String.format("数据包扫描完成，共找到 %d 个有效数据包", datapacks.size()));
            
        } catch (Exception e) {
            logManager.logError("DatapackScanner", "扫描数据包时发生错误", e);
        }
        
        return datapacks;
    }
    
    /**
     * 处理单个文件或目录条目
     * 
     * @param entryPath 条目路径
     * @return 数据包信息，如果不是有效数据包则返回null
     */
    private DatapackInfo processEntry(String entryPath) {
        try {
            // 跳过安装器自身
            if (isInstallerFile(entryPath)) {
                logManager.logDebug("DatapackScanner", "跳过安装器文件: " + entryPath);
                return null;
            }
            
            // 检查是否为ZIP文件
            if (FileUtils.isFile(entryPath) && isZipFile(entryPath)) {
                return processZipDatapack(entryPath);
            }
            
            // 检查是否为文件夹
            if (FileUtils.isDirectory(entryPath)) {
                return processFolderDatapack(entryPath);
            }
            
            logManager.logDebug("DatapackScanner", "跳过不支持的文件类型: " + entryPath);
            return null;
            
        } catch (Exception e) {
            logManager.logError("DatapackScanner", "处理条目时发生错误: " + entryPath, e);
            return null;
        }
    }
    
    /**
     * 处理ZIP格式的数据包
     * 
     * @param zipPath ZIP文件路径
     * @return 数据包信息，解析失败时返回null
     */
    private DatapackInfo processZipDatapack(String zipPath) {
        logManager.logDebug("DatapackScanner", "正在处理ZIP数据包: " + zipPath);
        
        // 验证是否为有效的数据包ZIP文件
        if (!YamlConfigParser.isValidDatapackZip(zipPath)) {
            logManager.logWarn("DatapackScanner", "无效的数据包ZIP文件: " + zipPath);
            return null;
        }
        
        // 解析配置文件
        DatapackInfo datapackInfo = YamlConfigParser.parseFromZipFile(zipPath);
        if (datapackInfo == null) {
            logManager.logWarn("DatapackScanner", "无法解析ZIP数据包配置: " + zipPath);
            return null;
        }
        
        logManager.logInfo("DatapackScanner", 
                String.format("成功解析ZIP数据包: %s (%s)", 
                        datapackInfo.getFullName(), zipPath));
        
        return datapackInfo;
    }
    
    /**
     * 处理文件夹格式的数据包
     * 
     * @param folderPath 文件夹路径
     * @return 数据包信息，解析失败时返回null
     */
    private DatapackInfo processFolderDatapack(String folderPath) {
        logManager.logDebug("DatapackScanner", "正在处理文件夹数据包: " + folderPath);
        
        // 验证是否为有效的数据包文件夹
        if (!YamlConfigParser.isValidDatapackFolder(folderPath)) {
            logManager.logWarn("DatapackScanner", "无效的数据包文件夹: " + folderPath);
            return null;
        }
        
        // 解析配置文件
        DatapackInfo datapackInfo = YamlConfigParser.parseFromFolder(folderPath);
        if (datapackInfo == null) {
            logManager.logWarn("DatapackScanner", "无法解析文件夹数据包配置: " + folderPath);
            return null;
        }
        
        logManager.logInfo("DatapackScanner", 
                String.format("成功解析文件夹数据包: %s (%s)", 
                        datapackInfo.getFullName(), folderPath));
        
        return datapackInfo;
    }
    
    /**
     * 检查文件是否为ZIP格式
     * 
     * @param filePath 文件路径
     * @return 是否为ZIP文件
     */
    private boolean isZipFile(String filePath) {
        String extension = FileUtils.getFileExtension(filePath);
        return ZIP_EXTENSION.equalsIgnoreCase(extension);
    }
    
    /**
     * 检查是否为安装器文件（避免扫描自身）
     * 
     * @param filePath 文件路径
     * @return 是否为安装器文件
     */
    private boolean isInstallerFile(String filePath) {
        String fileName = getFileName(filePath);
        return fileName.contains(INSTALLER_FILE_PATTERN);
    }
    
    /**
     * 从完整路径中提取文件名
     * 
     * @param filePath 文件路径
     * @return 文件名
     */
    private String getFileName(String filePath) {
        if (filePath == null) {
            return "";
        }
        
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastSeparatorIndex >= 0 && lastSeparatorIndex < filePath.length() - 1) {
            return filePath.substring(lastSeparatorIndex + 1);
        }
        
        return filePath;
    }
    
    /**
     * 验证数据包的完整性
     * 检查数据包是否包含必要的信息
     * 
     * @param datapackInfo 数据包信息
     * @return 验证结果
     */
    public boolean validateDatapack(DatapackInfo datapackInfo) {
        if (datapackInfo == null) {
            logManager.logError("DatapackScanner", "数据包信息为空");
            return false;
        }
        
        // 检查项目名称
        if (datapackInfo.getProjectName() == null || datapackInfo.getProjectName().trim().isEmpty()) {
            logManager.logError("DatapackScanner", "数据包缺少项目名称");
            return false;
        }
        
        // 检查版本号
        if (datapackInfo.getVersion() == null || datapackInfo.getVersion().trim().isEmpty()) {
            logManager.logError("DatapackScanner", "数据包缺少版本号: " + datapackInfo.getProjectName());
            return false;
        }
        
        // 检查文件路径
        if (datapackInfo.getFilePath() == null || !FileUtils.exists(datapackInfo.getFilePath())) {
            logManager.logError("DatapackScanner", "数据包文件路径无效: " + datapackInfo.getProjectName());
            return false;
        }
        
        // 验证项目全称是否为有效文件名
        if (!FileUtils.isValidFileName(datapackInfo.getFullName())) {
            logManager.logError("DatapackScanner", 
                    "数据包项目全称包含非法字符: " + datapackInfo.getFullName());
            return false;
        }
        
        logManager.logDebug("DatapackScanner", "数据包验证通过: " + datapackInfo.getFullName());
        return true;
    }
    
    /**
     * 过滤并返回有效的数据包列表
     * 
     * @param datapacks 原始数据包列表
     * @return 过滤后的有效数据包列表
     */
    public List<DatapackInfo> filterValidDatapacks(List<DatapackInfo> datapacks) {
        List<DatapackInfo> validDatapacks = new ArrayList<>();
        
        for (DatapackInfo datapack : datapacks) {
            if (validateDatapack(datapack)) {
                validDatapacks.add(datapack);
            } else {
                logManager.logWarn("DatapackScanner", 
                        "过滤掉无效数据包: " + (datapack != null ? datapack.toString() : "null"));
            }
        }
        
        logManager.logInfo("DatapackScanner", 
                String.format("数据包过滤完成，有效数据包: %d/%d", 
                        validDatapacks.size(), datapacks.size()));
        
        return validDatapacks;
    }
    
    /**
     * 检查是否存在重复的项目名称
     * 
     * @param datapacks 数据包列表
     * @return 是否存在重复项目
     */
    public boolean hasDuplicateProjects(List<DatapackInfo> datapacks) {
        List<String> projectNames = new ArrayList<>();
        
        for (DatapackInfo datapack : datapacks) {
            String projectName = datapack.getProjectName();
            if (projectNames.contains(projectName)) {
                logManager.logWarn("DatapackScanner", "发现重复的项目名称: " + projectName);
                return true;
            }
            projectNames.add(projectName);
        }
        
        return false;
    }
    
    /**
     * 获取扫描统计信息
     * 
     * @param datapacks 数据包列表
     * @return 统计信息字符串
     */
    public String getScanStatistics(List<DatapackInfo> datapacks) {
        if (datapacks.isEmpty()) {
            return "扫描统计: 未找到任何数据包";
        }
        
        int zipCount = 0;
        int folderCount = 0;
        int withHardDeps = 0;
        int withSoftDeps = 0;
        
        for (DatapackInfo datapack : datapacks) {
            if (datapack.isZipFile()) {
                zipCount++;
            } else {
                folderCount++;
            }
            
            if (!datapack.getHardDependencies().isEmpty()) {
                withHardDeps++;
            }
            
            if (!datapack.getSoftDependencies().isEmpty()) {
                withSoftDeps++;
            }
        }
        
        return String.format("扫描统计: 总计 %d 个数据包 (ZIP: %d, 文件夹: %d, 有硬依赖: %d, 有软依赖: %d)",
                datapacks.size(), zipCount, folderCount, withHardDeps, withSoftDeps);
    }
}