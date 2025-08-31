package com.aeterhilrin.datapackinstaller.nbt;

import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;
import com.aeterhilrin.datapackinstaller.model.LevelDatData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * NBT文件处理器
 * 负责读取和修改Minecraft存档的level.dat文件
 * 
 * @author AeterHilrin
 */
public class NBTProcessor {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** NBT验证器 */
    private final NBTValidator nbtValidator;
    
    /**
     * 构造函数
     */
    public NBTProcessor() {
        this.nbtValidator = new NBTValidator();
    }
    
    /**
     * 读取level.dat文件中的数据包配置
     * 
     * @param levelDatPath level.dat文件路径
     * @return 数据包配置信息，读取失败时返回null
     * @throws IOException 文件读取异常
     */
    public LevelDatData readLevelDat(String levelDatPath) throws IOException {
        logManager.logInfo("NBTProcessor", "开始读取level.dat文件: " + levelDatPath);
        
        try {
            // 验证文件完整性和权限
            if (!nbtValidator.validateFileIntegrity(levelDatPath)) {
                throw new IOException("level.dat文件完整性验证失败");
            }
            
            if (!nbtValidator.checkFilePermissions(levelDatPath)) {
                throw new IOException("level.dat文件权限检查失败");
            }
            
            // 验证NBT结构
            if (!nbtValidator.validateStructure(levelDatPath)) {
                throw new IOException("level.dat文件结构验证失败");
            }
            
            // 读取NBT文件
            File levelDatFile = new File(levelDatPath);
            NamedTag namedTag = NBTUtil.read(levelDatFile);
            
            if (namedTag == null) {
                throw new IOException("无法读取NBT文件: " + levelDatPath);
            }
            
            CompoundTag root = (CompoundTag) namedTag.getTag();
            LevelDatData levelDatData = new LevelDatData(root);
            
            logManager.logInfo("NBTProcessor", 
                    String.format("成功读取level.dat文件，启用数据包: %d 个，禁用数据包: %d 个",
                            levelDatData.getEnabledPackCount(), 
                            levelDatData.getDisabledPackCount()));
            
            return levelDatData;
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "读取level.dat文件失败: " + levelDatPath, e);
            throw new IOException("无法读取NBT文件: " + levelDatPath, e);
        }
    }
    
    /**
     * 更新level.dat文件中的数据包配置
     * 
     * @param levelDatPath level.dat文件路径
     * @param newDatapacks 新的数据包列表
     * @throws IOException 文件写入异常
     */
    public void updateLevelDat(String levelDatPath, List<DatapackInfo> newDatapacks) 
            throws IOException {
        
        logManager.logInfo("NBTProcessor", 
                String.format("开始更新level.dat文件，添加 %d 个数据包", newDatapacks.size()));
        
        try {
            // 创建备份
            createBackup(levelDatPath);
            
            // 读取现有数据
            LevelDatData levelDatData = readLevelDat(levelDatPath);
            
            // 移除冲突的数据包项目
            levelDatData.removeConflictingPacks(newDatapacks);
            logManager.logInfo("NBTProcessor", "已清理冲突的数据包条目");
            
            // 添加新的数据包条目
            levelDatData.addDatapacks(newDatapacks);
            
            for (DatapackInfo datapack : newDatapacks) {
                logManager.logDebug("NBTProcessor", "添加数据包条目: " + datapack.getFullName());
            }
            
            // 保存文件
            saveLevelDat(levelDatPath, levelDatData);
            
            logManager.logInfo("NBTProcessor", 
                    String.format("成功更新level.dat文件，总启用数据包: %d 个", 
                            levelDatData.getEnabledPackCount()));
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "更新level.dat文件失败: " + levelDatPath, e);
            
            // 尝试恢复备份
            if (!restoreBackup(levelDatPath)) {
                logManager.logError("NBTProcessor", "恢复备份文件也失败了！");
            }
            
            throw new IOException("无法写入NBT文件: " + levelDatPath, e);
        }
    }
    
    /**
     * 保存LevelDatData到文件
     * 
     * @param levelDatPath level.dat文件路径
     * @param levelDatData 数据包配置数据
     * @throws IOException 保存失败
     */
    private void saveLevelDat(String levelDatPath, LevelDatData levelDatData) throws IOException {
        try {
            File levelDatFile = new File(levelDatPath);
            
            // 创建NamedTag包装根标签
            NamedTag namedTag = new NamedTag("", levelDatData.getRootTag());
            
            // 写入文件
            NBTUtil.write(namedTag, levelDatFile);
            
            logManager.logDebug("NBTProcessor", "NBT文件写入完成");
            
        } catch (Exception e) {
            throw new IOException("保存NBT文件失败", e);
        }
    }
    
    /**
     * 创建level.dat文件的备份
     * 
     * @param levelDatPath level.dat文件路径
     * @return 备份是否成功
     */
    private boolean createBackup(String levelDatPath) {
        try {
            File originalFile = new File(levelDatPath);
            File backupFile = new File(levelDatPath + ".backup");
            
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            logManager.logInfo("NBTProcessor", "已创建level.dat备份文件: " + backupFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "创建备份文件失败", e);
            return false;
        }
    }
    
    /**
     * 恢复level.dat文件的备份
     * 
     * @param levelDatPath level.dat文件路径
     * @return 恢复是否成功
     */
    private boolean restoreBackup(String levelDatPath) {
        try {
            File originalFile = new File(levelDatPath);
            File backupFile = new File(levelDatPath + ".backup");
            
            if (!backupFile.exists()) {
                logManager.logError("NBTProcessor", "备份文件不存在: " + backupFile.getAbsolutePath());
                return false;
            }
            
            Files.copy(backupFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            logManager.logInfo("NBTProcessor", "已恢复level.dat备份文件");
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "恢复备份文件失败", e);
            return false;
        }
    }
    
    /**
     * 清理备份文件
     * 
     * @param levelDatPath level.dat文件路径
     */
    public void cleanupBackup(String levelDatPath) {
        try {
            File backupFile = new File(levelDatPath + ".backup");
            
            if (backupFile.exists() && backupFile.delete()) {
                logManager.logDebug("NBTProcessor", "已清理备份文件: " + backupFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            logManager.logWarn("NBTProcessor", "清理备份文件失败", e);
        }
    }
    
    /**
     * 验证数据包条目格式
     * 
     * @param datapack 数据包信息
     * @return 生成的数据包条目字符串
     */
    public String generateDatapackEntry(DatapackInfo datapack) {
        if (datapack == null) {
            throw new IllegalArgumentException("数据包信息不能为空");
        }
        
        String fullName = datapack.getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("数据包全称不能为空");
        }
        
        return "file/" + fullName;
    }
    
    /**
     * 检查数据包是否已在level.dat中启用
     * 
     * @param levelDatPath level.dat文件路径
     * @param datapackName 数据包名称
     * @return 是否已启用
     */
    public boolean isDatapackEnabled(String levelDatPath, String datapackName) {
        try {
            LevelDatData levelDatData = readLevelDat(levelDatPath);
            return levelDatData.isDatapackEnabled(datapackName);
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "检查数据包状态失败", e);
            return false;
        }
    }
    
    /**
     * 获取当前启用的数据包列表
     * 
     * @param levelDatPath level.dat文件路径
     * @return 启用的数据包条目列表
     */
    public List<String> getEnabledDatapacks(String levelDatPath) {
        try {
            LevelDatData levelDatData = readLevelDat(levelDatPath);
            return levelDatData.getEnabledPackEntries();
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "获取启用数据包列表失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前禁用的数据包列表
     * 
     * @param levelDatPath level.dat文件路径
     * @return 禁用的数据包条目列表
     */
    public List<String> getDisabledDatapacks(String levelDatPath) {
        try {
            LevelDatData levelDatData = readLevelDat(levelDatPath);
            return levelDatData.getDisabledPackEntries();
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "获取禁用数据包列表失败", e);
            return null;
        }
    }
    
    /**
     * 测试NBT文件处理功能
     * 用于验证NBT处理器是否正常工作
     * 
     * @param levelDatPath level.dat文件路径
     * @return 测试结果
     */
    public boolean testNBTProcessing(String levelDatPath) {
        logManager.logInfo("NBTProcessor", "开始测试NBT文件处理功能");
        
        try {
            // 测试读取
            LevelDatData levelDatData = readLevelDat(levelDatPath);
            if (levelDatData == null) {
                logManager.logError("NBTProcessor", "读取测试失败");
                return false;
            }
            
            // 测试数据结构验证
            if (!levelDatData.validateStructure()) {
                logManager.logError("NBTProcessor", "数据结构验证测试失败");
                return false;
            }
            
            logManager.logInfo("NBTProcessor", "NBT文件处理功能测试通过");
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTProcessor", "NBT文件处理功能测试失败", e);
            return false;
        }
    }
}