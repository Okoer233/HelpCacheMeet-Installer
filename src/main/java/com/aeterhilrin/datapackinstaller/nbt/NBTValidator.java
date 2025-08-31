package com.aeterhilrin.datapackinstaller.nbt;

import com.aeterhilrin.datapackinstaller.logging.LogManager;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.StringTag;

import java.io.File;

/**
 * NBT文件结构验证器
 * 确保level.dat文件具有正确的NBT结构
 * 
 * @author AeterHilrin
 */
public class NBTValidator {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /**
     * 验证level.dat文件结构是否正确
     * 
     * @param levelDatPath level.dat文件路径
     * @return 验证结果
     */
    public boolean validateStructure(String levelDatPath) {
        logManager.logInfo("NBTValidator", "开始验证level.dat文件结构: " + levelDatPath);
        
        try {
            // 检查文件是否存在
            File levelDatFile = new File(levelDatPath);
            if (!levelDatFile.exists() || !levelDatFile.isFile()) {
                logManager.logError("NBTValidator", "level.dat文件不存在: " + levelDatPath);
                return false;
            }
            
            // 读取NBT文件
            NamedTag namedTag = NBTUtil.read(levelDatFile);
            if (namedTag == null) {
                logManager.logError("NBTValidator", "无法读取NBT文件或文件为空");
                return false;
            }
            
            CompoundTag root = (CompoundTag) namedTag.getTag();
            if (root == null) {
                logManager.logError("NBTValidator", "NBT根标签为空或不是CompoundTag类型");
                return false;
            }
            
            // 验证必要的节点结构
            if (!validateDataNode(root)) {
                return false;
            }
            
            if (!validateDataPacksNode(root)) {
                return false;
            }
            
            if (!validateDataPacksLists(root)) {
                return false;
            }
            
            logManager.logInfo("NBTValidator", "level.dat文件结构验证通过");
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTValidator", "验证level.dat文件结构时发生错误", e);
            return false;
        }
    }
    
    /**
     * 验证Data节点是否存在
     * 
     * @param root NBT根标签
     * @return 验证结果
     */
    private boolean validateDataNode(CompoundTag root) {
        if (!root.containsKey("Data")) {
            logManager.logError("NBTValidator", "level.dat缺少Data节点");
            return false;
        }
        
        Object dataObj = root.get("Data");
        if (!(dataObj instanceof CompoundTag)) {
            logManager.logError("NBTValidator", "Data节点不是CompoundTag类型");
            return false;
        }
        
        logManager.logDebug("NBTValidator", "Data节点验证通过");
        return true;
    }
    
    /**
     * 验证DataPacks节点是否存在
     * 
     * @param root NBT根标签
     * @return 验证结果
     */
    private boolean validateDataPacksNode(CompoundTag root) {
        CompoundTag data = root.getCompoundTag("Data");
        
        if (!data.containsKey("DataPacks")) {
            logManager.logError("NBTValidator", "level.dat缺少DataPacks节点");
            return false;
        }
        
        Object dataPacksObj = data.get("DataPacks");
        if (!(dataPacksObj instanceof CompoundTag)) {
            logManager.logError("NBTValidator", "DataPacks节点不是CompoundTag类型");
            return false;
        }
        
        logManager.logDebug("NBTValidator", "DataPacks节点验证通过");
        return true;
    }
    
    /**
     * 验证DataPacks下的Enabled和Disabled列表
     * 
     * @param root NBT根标签
     * @return 验证结果
     */
    private boolean validateDataPacksLists(CompoundTag root) {
        CompoundTag data = root.getCompoundTag("Data");
        CompoundTag dataPacks = data.getCompoundTag("DataPacks");
        
        // 验证Enabled列表
        if (!validatePackList(dataPacks, "Enabled")) {
            return false;
        }
        
        // 验证Disabled列表
        if (!validatePackList(dataPacks, "Disabled")) {
            return false;
        }
        
        logManager.logDebug("NBTValidator", "DataPacks列表验证通过");
        return true;
    }
    
    /**
     * 验证数据包列表节点
     * 
     * @param dataPacks DataPacks节点
     * @param listName 列表名称（Enabled或Disabled）
     * @return 验证结果
     */
    private boolean validatePackList(CompoundTag dataPacks, String listName) {
        if (!dataPacks.containsKey(listName)) {
            logManager.logError("NBTValidator", 
                    "DataPacks节点缺少" + listName + "列表");
            return false;
        }
        
        Object listObj = dataPacks.get(listName);
        if (!(listObj instanceof ListTag)) {
            logManager.logError("NBTValidator", 
                    listName + "节点不是ListTag类型");
            return false;
        }
        
        @SuppressWarnings("unchecked")
        ListTag<StringTag> packList = (ListTag<StringTag>) listObj;
        
        // 验证列表中的所有元素都是StringTag
        for (int i = 0; i < packList.size(); i++) {
            Object element = packList.get(i);
            if (!(element instanceof StringTag)) {
                logManager.logError("NBTValidator", 
                        String.format("%s列表中第%d个元素不是StringTag类型", listName, i));
                return false;
            }
        }
        
        logManager.logDebug("NBTValidator", 
                String.format("%s列表验证通过，包含%d个条目", listName, packList.size()));
        return true;
    }
    
    /**
     * 验证NBT文件的基本完整性
     * 检查文件是否损坏或格式错误
     * 
     * @param levelDatPath level.dat文件路径
     * @return 验证结果
     */
    public boolean validateFileIntegrity(String levelDatPath) {
        logManager.logInfo("NBTValidator", "验证NBT文件完整性: " + levelDatPath);
        
        try {
            File levelDatFile = new File(levelDatPath);
            
            // 检查文件大小
            if (levelDatFile.length() == 0) {
                logManager.logError("NBTValidator", "level.dat文件为空");
                return false;
            }
            
            // 尝试读取NBT文件
            NamedTag namedTag = NBTUtil.read(levelDatFile);
            if (namedTag == null) {
                logManager.logError("NBTValidator", "无法解析NBT文件，可能文件已损坏");
                return false;
            }
            
            // 检查根标签名称
            String rootName = namedTag.getName();
            if (rootName == null || rootName.isEmpty()) {
                logManager.logWarn("NBTValidator", "NBT根标签名称为空");
            } else {
                logManager.logDebug("NBTValidator", "NBT根标签名称: " + rootName);
            }
            
            logManager.logInfo("NBTValidator", "NBT文件完整性验证通过");
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTValidator", "验证NBT文件完整性时发生错误", e);
            return false;
        }
    }
    
    /**
     * 检查level.dat文件权限
     * 
     * @param levelDatPath level.dat文件路径
     * @return 权限检查结果
     */
    public boolean checkFilePermissions(String levelDatPath) {
        try {
            File levelDatFile = new File(levelDatPath);
            
            if (!levelDatFile.canRead()) {
                logManager.logError("NBTValidator", "level.dat文件没有读取权限");
                return false;
            }
            
            if (!levelDatFile.canWrite()) {
                logManager.logError("NBTValidator", "level.dat文件没有写入权限");
                return false;
            }
            
            logManager.logDebug("NBTValidator", "level.dat文件权限检查通过");
            return true;
            
        } catch (Exception e) {
            logManager.logError("NBTValidator", "检查文件权限时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取level.dat文件的基本信息
     * 
     * @param levelDatPath level.dat文件路径
     * @return 文件信息字符串
     */
    public String getFileInfo(String levelDatPath) {
        try {
            File levelDatFile = new File(levelDatPath);
            
            if (!levelDatFile.exists()) {
                return "文件不存在: " + levelDatPath;
            }
            
            StringBuilder info = new StringBuilder();
            info.append("level.dat文件信息:\n");
            info.append("- 文件路径: ").append(levelDatPath).append("\n");
            info.append("- 文件大小: ").append(levelDatFile.length()).append(" 字节\n");
            info.append("- 最后修改: ").append(new java.util.Date(levelDatFile.lastModified())).append("\n");
            info.append("- 可读: ").append(levelDatFile.canRead()).append("\n");
            info.append("- 可写: ").append(levelDatFile.canWrite()).append("\n");
            
            // 尝试获取NBT信息
            try {
                NamedTag namedTag = NBTUtil.read(levelDatFile);
                if (namedTag != null) {
                    info.append("- NBT根标签: ").append(namedTag.getName()).append("\n");
                    
                    CompoundTag root = (CompoundTag) namedTag.getTag();
                    if (root.containsKey("Data")) {
                        CompoundTag data = root.getCompoundTag("Data");
                        if (data.containsKey("DataPacks")) {
                            CompoundTag dataPacks = data.getCompoundTag("DataPacks");
                            
                            if (dataPacks.containsKey("Enabled")) {
                                @SuppressWarnings("unchecked")
                                ListTag<StringTag> enabled = (ListTag<StringTag>) dataPacks.getListTag("Enabled");
                                info.append("- 启用的数据包数量: ").append(enabled.size()).append("\n");
                            }
                            
                            if (dataPacks.containsKey("Disabled")) {
                                @SuppressWarnings("unchecked")
                                ListTag<StringTag> disabled = (ListTag<StringTag>) dataPacks.getListTag("Disabled");
                                info.append("- 禁用的数据包数量: ").append(disabled.size()).append("\n");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                info.append("- NBT解析错误: ").append(e.getMessage()).append("\n");
            }
            
            return info.toString();
            
        } catch (Exception e) {
            return "获取文件信息失败: " + e.getMessage();
        }
    }
}