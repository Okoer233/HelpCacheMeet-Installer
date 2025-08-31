package com.aeterhilrin.datapackinstaller.utils;

import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * YAML配置文件解析器
 * 负责解析数据包中的defination.yml配置文件
 * 
 * @author AeterHilrin
 */
public class YamlConfigParser {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 配置文件名称 */
    private static final String CONFIG_FILE_NAME = "defination.yml";
    
    /**
     * 从ZIP文件中解析配置
     * 
     * @param zipFilePath ZIP文件路径
     * @return 数据包信息，解析失败时返回null
     */
    public static DatapackInfo parseFromZipFile(String zipFilePath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // 查找配置文件
            ZipEntry configEntry = zipFile.getEntry(CONFIG_FILE_NAME);
            if (configEntry == null) {
                logManager.logWarn("YamlConfigParser", 
                        "ZIP文件中未找到配置文件: " + zipFilePath);
                return null;
            }
            
            // 读取配置文件内容
            try (InputStream inputStream = zipFile.getInputStream(configEntry);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                
                DatapackInfo datapackInfo = parseConfigContent(reader);
                if (datapackInfo != null) {
                    datapackInfo.setFilePath(zipFilePath);
                    datapackInfo.setZipFile(true);
                    datapackInfo.setOriginalFileName(new File(zipFilePath).getName());
                }
                
                return datapackInfo;
            }
            
        } catch (IOException e) {
            logManager.logError("YamlConfigParser", 
                    "读取ZIP文件配置失败: " + zipFilePath, e);
            return null;
        }
    }
    
    /**
     * 从文件夹中解析配置
     * 
     * @param folderPath 文件夹路径
     * @return 数据包信息，解析失败时返回null
     */
    public static DatapackInfo parseFromFolder(String folderPath) {
        File configFile = new File(folderPath, CONFIG_FILE_NAME);
        
        if (!configFile.exists() || !configFile.isFile()) {
            logManager.logWarn("YamlConfigParser", 
                    "文件夹中未找到配置文件: " + folderPath);
            return null;
        }
        
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            DatapackInfo datapackInfo = parseConfigContent(reader);
            if (datapackInfo != null) {
                datapackInfo.setFilePath(folderPath);
                datapackInfo.setZipFile(false);
                datapackInfo.setOriginalFileName(new File(folderPath).getName());
            }
            
            return datapackInfo;
            
        } catch (IOException e) {
            logManager.logError("YamlConfigParser", 
                    "读取文件夹配置失败: " + folderPath, e);
            return null;
        }
    }
    
    /**
     * 解析配置文件内容
     * 
     * @param reader 文件读取器
     * @return 数据包信息，解析失败时返回null
     */
    private static DatapackInfo parseConfigContent(Reader reader) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(reader);
            
            if (config == null || config.isEmpty()) {
                logManager.logError("YamlConfigParser", "配置文件为空或格式错误");
                return null;
            }
            
            // 提取基本信息
            String projectName = extractStringValue(config, "项目名称");
            String version = extractStringValue(config, "版本号");
            
            if (projectName == null || projectName.trim().isEmpty()) {
                logManager.logError("YamlConfigParser", "配置文件中缺少项目名称");
                return null;
            }
            
            if (version == null || version.trim().isEmpty()) {
                logManager.logError("YamlConfigParser", "配置文件中缺少版本号");
                return null;
            }
            
            // 创建数据包信息对象
            DatapackInfo datapackInfo = new DatapackInfo();
            datapackInfo.setProjectName(projectName.trim());
            datapackInfo.setVersion(version.trim());
            
            // 解析依赖关系
            parseDependencies(config, datapackInfo);
            
            logManager.logInfo("YamlConfigParser", 
                    "成功解析配置: " + datapackInfo.getFullName());
            
            return datapackInfo;
            
        } catch (Exception e) {
            logManager.logError("YamlConfigParser", "解析YAML配置失败", e);
            return null;
        }
    }
    
    /**
     * 解析依赖关系配置
     * 
     * @param config 配置映射
     * @param datapackInfo 数据包信息对象
     */
    private static void parseDependencies(Map<String, Object> config, DatapackInfo datapackInfo) {
        // 尝试解析硬依赖（支持多种可能的字段名）
        String hardDeps = extractStringValue(config, "硬依赖");
        if (hardDeps == null) {
            hardDeps = extractStringValue(config, "依赖");
        }
        if (hardDeps == null) {
            hardDeps = extractStringValue(config, "dependencies");
        }
        
        if (hardDeps != null) {
            datapackInfo.parseHardDependencies(hardDeps);
        }
        
        // 尝试解析软依赖
        String softDeps = extractStringValue(config, "软依赖");
        if (softDeps == null) {
            softDeps = extractStringValue(config, "可选依赖");
        }
        if (softDeps == null) {
            softDeps = extractStringValue(config, "soft_dependencies");
        }
        
        if (softDeps != null) {
            datapackInfo.parseSoftDependencies(softDeps);
        }
        
        logManager.logDebug("YamlConfigParser", 
                String.format("项目 %s 的依赖解析完成，硬依赖: %d 个，软依赖: %d 个",
                        datapackInfo.getProjectName(),
                        datapackInfo.getHardDependencies().size(),
                        datapackInfo.getSoftDependencies().size()));
    }
    
    /**
     * 从配置映射中提取字符串值
     * 
     * @param config 配置映射
     * @param key 键名
     * @return 字符串值，不存在时返回null
     */
    private static String extractStringValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        
        return value.toString().trim();
    }
    
    /**
     * 验证数据包文件夹是否包含有效的数据包结构
     * 
     * @param folderPath 文件夹路径
     * @return 是否为有效的数据包
     */
    public static boolean isValidDatapackFolder(String folderPath) {
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        
        // 检查是否存在配置文件
        File configFile = new File(folder, CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            return false;
        }
        
        // 检查是否存在data文件夹（Minecraft数据包的标准结构）
        File dataFolder = new File(folder, "data");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            logManager.logWarn("YamlConfigParser", 
                    "数据包文件夹缺少data目录: " + folderPath);
            // 不强制要求data文件夹，只记录警告
        }
        
        return true;
    }
    
    /**
     * 验证ZIP文件是否包含有效的数据包结构
     * 
     * @param zipFilePath ZIP文件路径
     * @return 是否为有效的数据包
     */
    public static boolean isValidDatapackZip(String zipFilePath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // 检查是否存在配置文件
            ZipEntry configEntry = zipFile.getEntry(CONFIG_FILE_NAME);
            if (configEntry == null) {
                return false;
            }
            
            // 检查是否存在data文件夹
            boolean hasDataFolder = zipFile.stream()
                    .anyMatch(entry -> entry.getName().startsWith("data/"));
            
            if (!hasDataFolder) {
                logManager.logWarn("YamlConfigParser", 
                        "数据包ZIP文件缺少data目录: " + zipFilePath);
                // 不强制要求data文件夹，只记录警告
            }
            
            return true;
            
        } catch (IOException e) {
            logManager.logError("YamlConfigParser", 
                    "验证ZIP文件失败: " + zipFilePath, e);
            return false;
        }
    }
}