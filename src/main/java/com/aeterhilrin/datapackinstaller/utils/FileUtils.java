package com.aeterhilrin.datapackinstaller.utils;

import com.aeterhilrin.datapackinstaller.logging.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 * 提供文件和目录操作的实用方法
 * 
 * @author AeterHilrin
 */
public class FileUtils {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 检查是否为文件
     * 
     * @param filePath 文件路径
     * @return 是否为文件
     */
    public static boolean isFile(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path) && Files.isRegularFile(path);
    }
    
    /**
     * 检查是否为目录
     * 
     * @param dirPath 目录路径
     * @return 是否为目录
     */
    public static boolean isDirectory(String dirPath) {
        Path path = Paths.get(dirPath);
        return Files.exists(path) && Files.isDirectory(path);
    }
    
    /**
     * 创建目录（包括父目录）
     * 
     * @param dirPath 目录路径
     * @return 创建是否成功
     */
    public static boolean createDirectories(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
            return true;
        } catch (IOException e) {
            logManager.logError("FileUtils", "创建目录失败: " + dirPath, e);
            return false;
        }
    }
    
    /**
     * 删除文件或目录
     * 
     * @param path 文件或目录路径
     * @return 删除是否成功
     */
    public static boolean delete(String path) {
        try {
            Path filePath = Paths.get(path);
            
            if (Files.isDirectory(filePath)) {
                // 递归删除目录
                deleteDirectoryRecursively(filePath);
            } else {
                // 删除文件
                Files.deleteIfExists(filePath);
            }
            
            return true;
            
        } catch (IOException e) {
            logManager.logError("FileUtils", "删除文件失败: " + path, e);
            return false;
        }
    }
    
    /**
     * 递归删除目录
     * 
     * @param dir 目录路径
     * @throws IOException 删除失败
     */
    private static void deleteDirectoryRecursively(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * 重命名文件或目录
     * 
     * @param oldPath 旧路径
     * @param newPath 新路径
     * @return 重命名是否成功
     */
    public static boolean rename(String oldPath, String newPath) {
        try {
            Path source = Paths.get(oldPath);
            Path target = Paths.get(newPath);
            
            // 如果目标文件已存在，先删除
            if (Files.exists(target)) {
                if (!delete(newPath)) {
                    logManager.logError("FileUtils", "无法删除已存在的目标文件: " + newPath);
                    return false;
                }
            }
            
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
            
        } catch (IOException e) {
            logManager.logError("FileUtils", 
                    String.format("重命名失败: %s -> %s", oldPath, newPath), e);
            return false;
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param filePath 文件路径
     * @return 文件大小（字节），获取失败时返回-1
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            logManager.logError("FileUtils", "获取文件大小失败: " + filePath, e);
            return -1;
        }
    }
    
    /**
     * 列出目录中的所有文件和子目录
     * 
     * @param dirPath 目录路径
     * @return 文件和目录列表，获取失败时返回空列表
     */
    public static List<String> listDirectory(String dirPath) {
        List<String> files = new ArrayList<>();
        
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.isDirectory(dir)) {
                logManager.logWarn("FileUtils", "指定路径不是目录: " + dirPath);
                return files;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    files.add(entry.toString());
                }
            }
            
        } catch (IOException e) {
            logManager.logError("FileUtils", "列出目录内容失败: " + dirPath, e);
        }
        
        return files;
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名（不包含点号），没有扩展名时返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        
        return "";
    }
    
    /**
     * 获取不包含扩展名的文件名
     * 
     * @param fileName 文件名
     * @return 不包含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        
        return fileName;
    }
    
    /**
     * 检查文件名是否有效（不包含非法字符）
     * 
     * @param fileName 文件名
     * @return 文件名是否有效
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // Windows系统中的非法字符
        String[] invalidChars = {"<", ">", ":", "\"", "|", "?", "*"};
        for (String invalidChar : invalidChars) {
            if (fileName.contains(invalidChar)) {
                return false;
            }
        }
        
        // 检查保留名称（Windows）
        String[] reservedNames = {
            "CON", "PRN", "AUX", "NUL", 
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        };
        
        String nameWithoutExt = getFileNameWithoutExtension(fileName).toUpperCase();
        for (String reserved : reservedNames) {
            if (reserved.equals(nameWithoutExt)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 安全地构建文件路径
     * 防止路径遍历攻击
     * 
     * @param basePath 基础路径
     * @param fileName 文件名
     * @return 安全的文件路径
     */
    public static String safeBuildPath(String basePath, String fileName) {
        try {
            Path base = Paths.get(basePath).normalize();
            Path file = base.resolve(fileName).normalize();
            
            // 确保解析后的路径仍在基础路径下
            if (!file.startsWith(base)) {
                throw new IllegalArgumentException("文件路径超出了基础目录范围");
            }
            
            return file.toString();
            
        } catch (Exception e) {
            logManager.logError("FileUtils", 
                    String.format("构建安全路径失败: %s + %s", basePath, fileName), e);
            return null;
        }
    }
    
    /**
     * 获取文件的父目录路径
     * 
     * @param filePath 文件路径
     * @return 父目录路径，获取失败时返回null
     */
    public static String getParentDirectory(String filePath) {
        try {
            Path parent = Paths.get(filePath).getParent();
            return parent != null ? parent.toString() : null;
        } catch (Exception e) {
            logManager.logError("FileUtils", "获取父目录失败: " + filePath, e);
            return null;
        }
    }
    
    /**
     * 检查目录是否为空
     * 
     * @param dirPath 目录路径
     * @return 目录是否为空，如果不是目录则返回false
     */
    public static boolean isDirectoryEmpty(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.isDirectory(dir)) {
                return false;
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                return !stream.iterator().hasNext();
            }
            
        } catch (IOException e) {
            logManager.logError("FileUtils", "检查目录是否为空失败: " + dirPath, e);
            return false;
        }
    }
}