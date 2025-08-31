package com.aeterhilrin.datapackinstaller.core;

import com.aeterhilrin.datapackinstaller.exception.CyclicDependencyException;
import com.aeterhilrin.datapackinstaller.exception.DependencyResolutionException;
import com.aeterhilrin.datapackinstaller.exception.EnvironmentValidationException;
import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;
import com.aeterhilrin.datapackinstaller.nbt.NBTProcessor;
import com.aeterhilrin.datapackinstaller.ui.UIManager;

import java.util.List;

/**
 * 主要的逻辑
 * 协调各个组件完成数据包安装流程
 * 
 * @author AeterHilrin
 */
public class MainController {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** UI管理器 */
    private final UIManager uiManager;
    
    /** 环境检测器 */
    private final EnvironmentChecker environmentChecker;
    
    /** 数据包扫描器 */
    private final DatapackScanner datapackScanner;
    
    /** 依赖解析器 */
    private final DependencyResolver dependencyResolver;
    
    /** NBT处理器 */
    private final NBTProcessor nbtProcessor;
    
    /**
     * 构造函数
     */
    public MainController() {
        this.uiManager = UIManager.getInstance();
        this.environmentChecker = new EnvironmentChecker();
        this.datapackScanner = new DatapackScanner();
        this.dependencyResolver = new DependencyResolver();
        this.nbtProcessor = new NBTProcessor();
    }
    
    /**
     * 运行主业务流程
     */
    public void run() {
        logManager.logInfo("MainController", "开始执行主业务流程");
        
        try {
            // 步骤1：显示协议确认窗口
            if (!showLicenseAgreement()) {
                logManager.logInfo("MainController", "用户不同意协议，退出程序");
                uiManager.exitApplication(0);
                return;
            }
            
            // 步骤2：环境检测
            if (!checkEnvironment()) {
                showEnvironmentErrorAndExit();
                return;
            }
            
            // 步骤3：扫描数据包
            List<DatapackInfo> datapacks = scanDatapacks();
            if (datapacks.isEmpty()) {
                showNoDatapacksMessageAndExit();
                return;
            }
            
            // 步骤4：处理依赖关系
            List<DatapackInfo> sortedDatapacks;
            try {
                sortedDatapacks = processDependencies(datapacks);
            } catch (DependencyResolutionException | CyclicDependencyException e) {
                showDependencyErrorAndExit(e.getMessage());
                return;
            }
            
            // 步骤5：重命名数据包文件
            renameDatapackFiles(sortedDatapacks);
            
            // 步骤6：更新level.dat文件
            try {
                updateLevelDat(sortedDatapacks);
            } catch (Exception e) {
                showFileOperationErrorAndExit(e.getMessage());
                return;
            }
            
            // 步骤7：显示完成提示
            showCompletionMessage(sortedDatapacks);
            
            logManager.logInfo("MainController", "主业务流程执行完成");
            
        } catch (Exception e) {
            logManager.logError("MainController", "主业务流程执行失败", e);
            showUnexpectedErrorAndExit(e.getMessage());
        } finally {
            // 清理工作
            cleanup();
        }
    }
    
    /**
     * 显示协议确认窗口
     * 
     * @return 用户是否同意协议
     */
    private boolean showLicenseAgreement() {
        logManager.logInfo("MainController", "显示软件使用协议");
        return uiManager.showLicenseDialog();
    }
    
    /**
     * 检查运行环境
     * 
     * @return 环境检查是否通过
     */
    private boolean checkEnvironment() {
        logManager.logInfo("MainController", "开始环境检测");
        
        try {
            environmentChecker.validateEnvironment();
            logManager.logInfo("MainController", "环境检测通过");
            return true;
            
        } catch (EnvironmentValidationException e) {
            logManager.logError("MainController", "环境检测失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 扫描数据包
     * 
     * @return 有效的数据包列表
     */
    private List<DatapackInfo> scanDatapacks() {
        logManager.logInfo("MainController", "开始扫描数据包");
        
        String datapacksPath = environmentChecker.getDatapacksFolder();
        List<DatapackInfo> datapacks = datapackScanner.scanDatapacks(datapacksPath);
        
        // 过滤有效的数据包
        List<DatapackInfo> validDatapacks = datapackScanner.filterValidDatapacks(datapacks);
        
        logManager.logInfo("MainController", 
                String.format("数据包扫描完成，发现 %d 个有效数据包", validDatapacks.size()));
        
        // 记录扫描统计信息
        String statistics = datapackScanner.getScanStatistics(validDatapacks);
        logManager.logInfo("MainController", statistics);
        
        return validDatapacks;
    }
    
    /**
     * 处理依赖关系
     * 
     * @param datapacks 数据包列表
     * @return 按依赖关系排序后的数据包列表
     * @throws DependencyResolutionException 依赖解析失败
     * @throws CyclicDependencyException 循环依赖
     */
    private List<DatapackInfo> processDependencies(List<DatapackInfo> datapacks) 
            throws DependencyResolutionException, CyclicDependencyException {
        
        logManager.logInfo("MainController", "开始处理依赖关系");
        
        // 记录依赖统计信息
        String statistics = dependencyResolver.analyzeDependencyStatistics(datapacks);
        logManager.logInfo("MainController", statistics);
        
        // 解析依赖关系并排序
        List<DatapackInfo> sortedDatapacks = dependencyResolver.resolveDependencies(datapacks);
        
        logManager.logInfo("MainController", "依赖关系处理完成");
        
        return sortedDatapacks;
    }
    
    /**
     * 重命名数据包文件
     * 
     * @param datapacks 数据包列表
     */
    private void renameDatapackFiles(List<DatapackInfo> datapacks) {
        logManager.logInfo("MainController", "开始重命名数据包文件");
        
        int successCount = 0;
        int failureCount = 0;
        
        for (DatapackInfo datapack : datapacks) {
            try {
                if (datapack.rename()) {
                    successCount++;
                    logManager.logInfo("MainController", 
                            String.format("成功重命名: %s -> %s", 
                                    datapack.getOriginalFileName(), 
                                    datapack.getFullName()));
                } else {
                    failureCount++;
                    logManager.logWarn("MainController", 
                            "重命名失败: " + datapack.getOriginalFileName());
                }
            } catch (Exception e) {
                failureCount++;
                logManager.logError("MainController", 
                        "重命名时发生错误: " + datapack.getOriginalFileName(), e);
            }
        }
        
        logManager.logInfo("MainController", 
                String.format("文件重命名完成，成功: %d，失败: %d", successCount, failureCount));
    }
    
    /**
     * 更新level.dat文件
     * 
     * @param datapacks 数据包列表
     * @throws Exception 更新失败
     */
    private void updateLevelDat(List<DatapackInfo> datapacks) throws Exception {
        logManager.logInfo("MainController", "开始更新level.dat文件");
        
        String levelDatPath = environmentChecker.getLevelDatFile();
        if (levelDatPath == null) {
            throw new Exception("找不到level.dat文件");
        }
        
        // 测试NBT处理功能
        if (!nbtProcessor.testNBTProcessing(levelDatPath)) {
            throw new Exception("NBT文件处理功能测试失败");
        }
        
        // 更新NBT文件
        nbtProcessor.updateLevelDat(levelDatPath, datapacks);
        
        logManager.logInfo("MainController", "level.dat文件更新完成");
        
        // 清理备份文件
        nbtProcessor.cleanupBackup(levelDatPath);
    }
    
    /**
     * 显示完成消息
     * 
     * @param datapacks 已安装的数据包列表
     */
    private void showCompletionMessage(List<DatapackInfo> datapacks) {
        StringBuilder message = new StringBuilder();
        message.append("数据包安装完成！\n\n");
        message.append("已成功安装 ").append(datapacks.size()).append(" 个数据包：\n");
        
        for (int i = 0; i < datapacks.size(); i++) {
            DatapackInfo datapack = datapacks.get(i);
            message.append(String.format("%d. %s\n", i + 1, datapack.getFullName()));
        }
        
        message.append("\n安装顺序已根据依赖关系优化。");
        
        uiManager.showSuccessMessage(message.toString());
    }
    
    /**
     * 显示环境错误并退出
     */
    private void showEnvironmentErrorAndExit() {
        String message = "环境检验不通过，请放置于存档中！\n\n" +
                "请确保：\n" +
                "• 软件与datapacks文件夹在同一目录\n" +
                "• 软件与level.dat文件在同一目录\n" +
                "• 具有必要的文件读写权限";
        
        uiManager.showErrorMessage(message);
        uiManager.exitWithError();
    }
    
    /**
     * 显示无数据包消息并退出
     */
    private void showNoDatapacksMessageAndExit() {
        String message = "不存在需要进行初始化安装的数据包！\n\n" +
                "请确保：\n" +
                "• datapacks文件夹中包含有效的数据包\n" +
                "• 数据包包含defination.yml配置文件\n" +
                "• 配置文件格式正确";
        
        uiManager.showWarningMessage(message);
        uiManager.exitApplication();
    }
    
    /**
     * 显示依赖错误并退出
     * 
     * @param errorMessage 错误消息
     */
    private void showDependencyErrorAndExit(String errorMessage) {
        String message = "依赖关系处理失败！\n\n" +
                "错误详情：\n" + errorMessage + "\n\n" +
                "请检查数据包的依赖配置是否正确。";
        
        uiManager.showErrorMessage(message);
        uiManager.exitWithError();
    }
    
    /**
     * 显示文件操作错误并退出
     * 
     * @param errorMessage 错误消息
     */
    private void showFileOperationErrorAndExit(String errorMessage) {
        String message = "文件操作失败！\n\n" +
                "错误详情：\n" + errorMessage + "\n\n" +
                "请检查文件权限和磁盘空间。";
        
        uiManager.showErrorMessage(message);
        uiManager.exitWithError();
    }
    
    /**
     * 显示意外错误并退出
     * 
     * @param errorMessage 错误消息
     */
    private void showUnexpectedErrorAndExit(String errorMessage) {
        String message = "程序运行时发生意外错误！\n\n" +
                "错误详情：\n" + errorMessage + "\n\n" +
                "请查看日志文件以获取更多信息。";
        
        uiManager.showErrorMessage(message);
        uiManager.exitWithError();
    }
    
    /**
     * 清理工作
     */
    private void cleanup() {
        try {
            logManager.logInfo("MainController", "执行清理工作");
            
            // 关闭日志系统
            logManager.shutdown();
            
        } catch (Exception e) {
            System.err.println("清理工作时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取环境信息摘要
     * 
     * @return 环境信息
     */
    public String getEnvironmentSummary() {
        return environmentChecker.getEnvironmentSummary();
    }
    
    /**
     * 检查是否有重复的项目名称
     * 
     * @param datapacks 数据包列表
     * @return 是否有重复项目
     */
    private boolean checkDuplicateProjects(List<DatapackInfo> datapacks) {
        return datapackScanner.hasDuplicateProjects(datapacks);
    }
}