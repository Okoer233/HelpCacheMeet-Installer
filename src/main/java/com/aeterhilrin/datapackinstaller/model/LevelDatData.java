package com.aeterhilrin.datapackinstaller.model;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * level.dat文件数据模型类
 * 用于表示和操作Minecraft存档中的数据包配置
 * 
 * @author AeterHilrin
 */
public class LevelDatData {
    
    /** 根NBT标签 */
    private CompoundTag rootTag;
    
    /** 启用的数据包列表 */
    private ListTag<StringTag> enabledPacks;
    
    /** 禁用的数据包列表 */
    private ListTag<StringTag> disabledPacks;
    
    /**
     * 构造函数
     * 
     * @param rootTag NBT根标签
     */
    public LevelDatData(CompoundTag rootTag) {
        this.rootTag = rootTag;
        this.extractDatapackLists();
    }
    
    /**
     * 从NBT结构中提取数据包列表
     */
    @SuppressWarnings("unchecked")
    private void extractDatapackLists() {
        try {
            CompoundTag data = rootTag.getCompoundTag("Data");
            CompoundTag dataPacks = data.getCompoundTag("DataPacks");
            
            this.enabledPacks = (ListTag<StringTag>) dataPacks.getListTag("Enabled");
            this.disabledPacks = (ListTag<StringTag>) dataPacks.getListTag("Disabled");
            
            // 如果列表不存在，创建新的空列表
            if (this.enabledPacks == null) {
                this.enabledPacks = new ListTag<>(StringTag.class);
                dataPacks.put("Enabled", this.enabledPacks);
            }
            
            if (this.disabledPacks == null) {
                this.disabledPacks = new ListTag<>(StringTag.class);
                dataPacks.put("Disabled", this.disabledPacks);
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的level.dat NBT结构", e);
        }
    }
    
    /**
     * 移除与新数据包冲突的现有条目
     * 如果现有条目包含新数据包的项目名称，则将其移除
     * 
     * @param newDatapacks 要安装的新数据包列表
     */
    public void removeConflictingPacks(List<DatapackInfo> newDatapacks) {
        Set<String> projectNames = newDatapacks.stream()
                .map(DatapackInfo::getProjectName)
                .collect(Collectors.toSet());
        
        // 从启用列表中移除冲突项目
        removeConflictingFromList(enabledPacks, projectNames);
        
        // 从禁用列表中移除冲突项目
        removeConflictingFromList(disabledPacks, projectNames);
    }
    
    /**
     * 从指定列表中移除冲突的条目
     * 
     * @param packList 数据包列表
     * @param projectNames 项目名称集合
     */
    private void removeConflictingFromList(ListTag<StringTag> packList, Set<String> projectNames) {
        Iterator<StringTag> iterator = packList.iterator();
        while (iterator.hasNext()) {
            String existingEntry = iterator.next().getValue();
            
            // 检查是否与新项目名称冲突
            for (String projectName : projectNames) {
                if (existingEntry.contains(projectName)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
    
    /**
     * 添加数据包到启用列表
     * 
     * @param datapack 要添加的数据包
     */
    public void addDatapack(DatapackInfo datapack) {
        String entry = "file/" + datapack.getFullName();
        enabledPacks.add(new StringTag(entry));
    }
    
    /**
     * 批量添加数据包到启用列表
     * 
     * @param datapacks 要添加的数据包列表
     */
    public void addDatapacks(List<DatapackInfo> datapacks) {
        for (DatapackInfo datapack : datapacks) {
            addDatapack(datapack);
        }
    }
    
    /**
     * 获取所有启用的数据包条目
     * 
     * @return 启用的数据包条目列表
     */
    public List<String> getEnabledPackEntries() {
        List<String> entries = new ArrayList<>();
        for (StringTag tag : enabledPacks) {
            entries.add(tag.getValue());
        }
        return entries;
    }
    
    /**
     * 获取所有禁用的数据包条目
     * 
     * @return 禁用的数据包条目列表
     */
    public List<String> getDisabledPackEntries() {
        List<String> entries = new ArrayList<>();
        for (StringTag tag : disabledPacks) {
            entries.add(tag.getValue());
        }
        return entries;
    }
    
    /**
     * 检查指定数据包是否已启用
     * 
     * @param datapackName 数据包名称
     * @return 是否已启用
     */
    public boolean isDatapackEnabled(String datapackName) {
        for (StringTag tag : enabledPacks) {
            if (tag.getValue().contains(datapackName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 移除指定的数据包条目
     * 
     * @param datapackName 要移除的数据包名称
     * @return 是否成功移除
     */
    public boolean removeDatapack(String datapackName) {
        boolean removed = false;
        
        // 从启用列表中移除
        Iterator<StringTag> enabledIterator = enabledPacks.iterator();
        while (enabledIterator.hasNext()) {
            if (enabledIterator.next().getValue().contains(datapackName)) {
                enabledIterator.remove();
                removed = true;
            }
        }
        
        // 从禁用列表中移除
        Iterator<StringTag> disabledIterator = disabledPacks.iterator();
        while (disabledIterator.hasNext()) {
            if (disabledIterator.next().getValue().contains(datapackName)) {
                disabledIterator.remove();
                removed = true;
            }
        }
        
        return removed;
    }
    
    /**
     * 获取启用数据包的数量
     * 
     * @return 启用数据包数量
     */
    public int getEnabledPackCount() {
        return enabledPacks.size();
    }
    
    /**
     * 获取禁用数据包的数量
     * 
     * @return 禁用数据包数量
     */
    public int getDisabledPackCount() {
        return disabledPacks.size();
    }
    
    /**
     * 验证数据结构的完整性
     * 
     * @return 验证结果
     */
    public boolean validateStructure() {
        try {
            // 检查必要的NBT结构
            if (!rootTag.containsKey("Data")) {
                return false;
            }
            
            CompoundTag data = rootTag.getCompoundTag("Data");
            if (!data.containsKey("DataPacks")) {
                return false;
            }
            
            CompoundTag dataPacks = data.getCompoundTag("DataPacks");
            if (!dataPacks.containsKey("Enabled") || !dataPacks.containsKey("Disabled")) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    // Getter方法
    
    public CompoundTag getRootTag() {
        return rootTag;
    }
    
    public ListTag<StringTag> getEnabledPacks() {
        return enabledPacks;
    }
    
    public ListTag<StringTag> getDisabledPacks() {
        return disabledPacks;
    }
    
    @Override
    public String toString() {
        return "LevelDatData{" +
                "enabledPackCount=" + getEnabledPackCount() +
                ", disabledPackCount=" + getDisabledPackCount() +
                '}';
    }
}