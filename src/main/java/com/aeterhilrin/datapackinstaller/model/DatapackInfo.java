package com.aeterhilrin.datapackinstaller.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据包信息模型类
 * 存储数据包的基本信息和依赖关系
 * 
 * @author AeterHilrin
 */
public class DatapackInfo {
    
    /** 项目名称 */
    private String projectName;
    
    /** 版本号 */
    private String version;
    
    /** 硬依赖列表 */
    private List<String> hardDependencies;
    
    /** 软依赖列表 */
    private List<String> softDependencies;
    
    /** 文件路径 */
    private String filePath;
    
    /** 是否为ZIP文件格式 */
    private boolean isZipFile;
    
    /** 原始文件名 */
    private String originalFileName;
    
    /**
     * 构造函数
     */
    public DatapackInfo() {
        this.hardDependencies = new ArrayList<>();
        this.softDependencies = new ArrayList<>();
    }
    
    /**
     * 构造函数
     * 
     * @param projectName 项目名称
     * @param version 版本号
     * @param filePath 文件路径
     * @param isZipFile 是否为ZIP文件
     */
    public DatapackInfo(String projectName, String version, String filePath, boolean isZipFile) {
        this();
        this.projectName = projectName;
        this.version = version;
        this.filePath = filePath;
        this.isZipFile = isZipFile;
        this.originalFileName = new File(filePath).getName();
    }
    
    /**
     * 获取项目全称（项目名 + 版本号）
     * 格式：项目名 版本号
     * 
     * @return 项目全称
     */
    public String getFullName() {
        if (projectName == null || version == null) {
            throw new IllegalStateException("项目名称或版本号不能为空");
        }
        return projectName + " " + version;
    }
    
    /**
     * 重命名数据包文件
     * 将文件重命名为：项目名 版本号.zip 或 项目名 版本号（文件夹）
     * 
     * @return 重命名是否成功
     */
    public boolean rename() {
        try {
            File currentFile = new File(filePath);
            if (!currentFile.exists()) {
                return false;
            }
            
            String newName = getFullName();
            if (isZipFile) {
                newName += ".zip";
            }
            
            File newFile = new File(currentFile.getParent(), newName);
            
            // 如果新文件名与当前文件名相同，则无需重命名
            if (currentFile.getName().equals(newName)) {
                return true;
            }
            
            // 如果目标文件已存在，先删除
            if (newFile.exists()) {
                if (isZipFile) {
                    newFile.delete();
                } else {
                    deleteDirectory(newFile);
                }
            }
            
            boolean success = currentFile.renameTo(newFile);
            if (success) {
                this.filePath = newFile.getAbsolutePath();
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递归删除目录
     * 
     * @param directory 要删除的目录
     * @return 删除是否成功
     */
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }
    
    /**
     * 解析依赖字符串并添加到硬依赖列表
     * 
     * @param dependenciesStr 依赖字符串，用空格分隔
     */
    public void parseHardDependencies(String dependenciesStr) {
        if (dependenciesStr == null || dependenciesStr.trim().isEmpty() || "无".equals(dependenciesStr.trim())) {
            return;
        }
        
        String[] deps = dependenciesStr.trim().split("\\s+");
        this.hardDependencies.addAll(Arrays.asList(deps));
    }
    
    /**
     * 解析依赖字符串并添加到软依赖列表
     * 
     * @param dependenciesStr 依赖字符串，用空格分隔
     */
    public void parseSoftDependencies(String dependenciesStr) {
        if (dependenciesStr == null || dependenciesStr.trim().isEmpty() || "无".equals(dependenciesStr.trim())) {
            return;
        }
        
        String[] deps = dependenciesStr.trim().split("\\s+");
        this.softDependencies.addAll(Arrays.asList(deps));
    }
    
    // Getter和Setter方法
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public List<String> getHardDependencies() {
        return new ArrayList<>(hardDependencies);
    }
    
    public void setHardDependencies(List<String> hardDependencies) {
        this.hardDependencies = new ArrayList<>(hardDependencies);
    }
    
    public List<String> getSoftDependencies() {
        return new ArrayList<>(softDependencies);
    }
    
    public void setSoftDependencies(List<String> softDependencies) {
        this.softDependencies = new ArrayList<>(softDependencies);
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public boolean isZipFile() {
        return isZipFile;
    }
    
    public void setZipFile(boolean zipFile) {
        isZipFile = zipFile;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    @Override
    public String toString() {
        return "DatapackInfo{" +
                "projectName='" + projectName + '\'' +
                ", version='" + version + '\'' +
                ", hardDependencies=" + hardDependencies +
                ", softDependencies=" + softDependencies +
                ", filePath='" + filePath + '\'' +
                ", isZipFile=" + isZipFile +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DatapackInfo that = (DatapackInfo) obj;
        return projectName != null ? projectName.equals(that.projectName) : that.projectName == null;
    }
    
    @Override
    public int hashCode() {
        return projectName != null ? projectName.hashCode() : 0;
    }
}