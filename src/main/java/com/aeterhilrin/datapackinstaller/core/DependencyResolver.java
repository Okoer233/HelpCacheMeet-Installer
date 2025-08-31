package com.aeterhilrin.datapackinstaller.core;

import com.aeterhilrin.datapackinstaller.exception.CyclicDependencyException;
import com.aeterhilrin.datapackinstaller.exception.DependencyResolutionException;
import com.aeterhilrin.datapackinstaller.logging.LogManager;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 依赖解析器
 * 负责验证数据包依赖关系并进行拓扑排序
 * 实现Kahn算法进行拓扑排序
 * 
 * @author AeterHilrin
 */
public class DependencyResolver {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /**
     * 解析并排序数据包依赖关系
     * 
     * @param datapacks 数据包列表
     * @return 按依赖关系排序后的数据包列表
     * @throws DependencyResolutionException 依赖解析失败
     * @throws CyclicDependencyException 存在循环依赖
     */
    public List<DatapackInfo> resolveDependencies(List<DatapackInfo> datapacks) 
            throws DependencyResolutionException, CyclicDependencyException {
        
        logManager.logInfo("DependencyResolver", "开始解析数据包依赖关系...");
        
        if (datapacks == null || datapacks.isEmpty()) {
            logManager.logWarn("DependencyResolver", "数据包列表为空");
            return new ArrayList<>();
        }
        
        try {
            // 验证硬依赖
            validateHardDependencies(datapacks);
            
            // 执行拓扑排序
            List<DatapackInfo> sortedDatapacks = performTopologicalSort(datapacks);
            
            logManager.logInfo("DependencyResolver", 
                    String.format("依赖关系解析完成，排序后数据包数量: %d", sortedDatapacks.size()));
            
            return sortedDatapacks;
            
        } catch (DependencyResolutionException | CyclicDependencyException e) {
            logManager.logError("DependencyResolver", "依赖解析失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logManager.logError("DependencyResolver", "依赖解析过程中发生未知错误", e);
            throw new DependencyResolutionException("依赖解析过程中发生未知错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证硬依赖是否满足
     * 
     * @param datapacks 数据包列表
     * @throws DependencyResolutionException 硬依赖验证失败
     */
    private void validateHardDependencies(List<DatapackInfo> datapacks) 
            throws DependencyResolutionException {
        
        logManager.logInfo("DependencyResolver", "验证硬依赖关系...");
        
        // 构建项目名称到数据包信息的映射
        Map<String, DatapackInfo> projectMap = buildProjectMap(datapacks);
        
        List<String> missingDependencies = new ArrayList<>();
        
        for (DatapackInfo datapack : datapacks) {
            for (String hardDep : datapack.getHardDependencies()) {
                if (!projectMap.containsKey(hardDep)) {
                    String error = String.format("项目 '%s' 的硬依赖 '%s' 不存在", 
                            datapack.getProjectName(), hardDep);
                    missingDependencies.add(error);
                    logManager.logError("DependencyResolver", error);
                }
            }
        }
        
        if (!missingDependencies.isEmpty()) {
            String errorMessage = "硬依赖验证失败:\n" + String.join("\n", missingDependencies);
            throw new DependencyResolutionException(errorMessage);
        }
        
        logManager.logInfo("DependencyResolver", "硬依赖验证通过");
    }
    
    /**
     * 执行拓扑排序（Kahn算法）
     * 
     * @param datapacks 数据包列表
     * @return 排序后的数据包列表
     * @throws CyclicDependencyException 存在循环依赖
     */
    public List<DatapackInfo> performTopologicalSort(List<DatapackInfo> datapacks) 
            throws CyclicDependencyException {
        
        logManager.logInfo("DependencyResolver", "执行拓扑排序...");
        
        // 构建项目映射
        Map<String, DatapackInfo> projectMap = buildProjectMap(datapacks);
        
        // 构建邻接表（依赖图）
        Map<String, List<String>> adjList = buildAdjacencyList(datapacks);
        
        // 计算入度
        Map<String, Integer> inDegree = calculateInDegree(adjList, projectMap.keySet());
        
        // Kahn算法执行拓扑排序
        Queue<String> queue = new LinkedList<>();
        List<String> result = new ArrayList<>();
        
        // 将入度为0的节点加入队列
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
                logManager.logDebug("DependencyResolver", 
                        "添加无依赖项目到队列: " + entry.getKey());
            }
        }
        
        // 执行排序
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            logManager.logDebug("DependencyResolver", "处理项目: " + current);
            
            // 更新邻接节点的入度
            List<String> neighbors = adjList.getOrDefault(current, new ArrayList<>());
            for (String neighbor : neighbors) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                    logManager.logDebug("DependencyResolver", 
                            "项目 " + neighbor + " 的依赖已满足，加入队列");
                }
            }
        }
        
        // 检查是否存在循环依赖
        if (result.size() != datapacks.size()) {
            List<String> remainingProjects = new ArrayList<>();
            for (DatapackInfo datapack : datapacks) {
                if (!result.contains(datapack.getProjectName())) {
                    remainingProjects.add(datapack.getProjectName());
                }
            }
            
            String errorMessage = "检测到循环依赖，涉及的项目: " + String.join(", ", remainingProjects);
            logManager.logError("DependencyResolver", errorMessage);
            throw new CyclicDependencyException(errorMessage);
        }
        
        // 将排序结果转换为DatapackInfo列表
        List<DatapackInfo> sortedDatapacks = convertToDatapackList(result, projectMap);
        
        logManager.logInfo("DependencyResolver", 
                "拓扑排序完成，排序顺序: " + 
                sortedDatapacks.stream().map(DatapackInfo::getProjectName).collect(Collectors.joining(" -> ")));
        
        return sortedDatapacks;
    }
    
    /**
     * 构建项目名称到数据包信息的映射
     * 
     * @param datapacks 数据包列表
     * @return 项目映射
     */
    private Map<String, DatapackInfo> buildProjectMap(List<DatapackInfo> datapacks) {
        Map<String, DatapackInfo> projectMap = new HashMap<>();
        
        for (DatapackInfo datapack : datapacks) {
            String projectName = datapack.getProjectName();
            if (projectMap.containsKey(projectName)) {
                logManager.logWarn("DependencyResolver", "发现重复的项目名称: " + projectName);
            }
            projectMap.put(projectName, datapack);
        }
        
        return projectMap;
    }
    
    /**
     * 构建邻接表（依赖图）
     * 注意：这里的邻接表表示"依赖关系"，A依赖B意味着B指向A
     * 
     * @param datapacks 数据包列表
     * @return 邻接表
     */
    private Map<String, List<String>> buildAdjacencyList(List<DatapackInfo> datapacks) {
        Map<String, List<String>> adjList = new HashMap<>();
        
        // 初始化邻接表
        for (DatapackInfo datapack : datapacks) {
            adjList.put(datapack.getProjectName(), new ArrayList<>());
        }
        
        // 构建依赖关系
        for (DatapackInfo datapack : datapacks) {
            String projectName = datapack.getProjectName();
            
            // 处理硬依赖
            for (String hardDep : datapack.getHardDependencies()) {
                if (adjList.containsKey(hardDep)) {
                    adjList.get(hardDep).add(projectName);
                    logManager.logDebug("DependencyResolver", 
                            String.format("添加硬依赖关系: %s -> %s", hardDep, projectName));
                }
            }
            
            // 处理软依赖
            for (String softDep : datapack.getSoftDependencies()) {
                if (adjList.containsKey(softDep)) {
                    adjList.get(softDep).add(projectName);
                    logManager.logDebug("DependencyResolver", 
                            String.format("添加软依赖关系: %s -> %s", softDep, projectName));
                }
            }
        }
        
        return adjList;
    }
    
    /**
     * 计算所有节点的入度
     * 
     * @param adjList 邻接表
     * @param allProjects 所有项目名称集合
     * @return 入度映射
     */
    private Map<String, Integer> calculateInDegree(Map<String, List<String>> adjList, Set<String> allProjects) {
        Map<String, Integer> inDegree = new HashMap<>();
        
        // 初始化所有项目的入度为0
        for (String project : allProjects) {
            inDegree.put(project, 0);
        }
        
        // 计算入度
        for (String project : allProjects) {
            List<String> dependents = adjList.get(project);
            for (String dependent : dependents) {
                inDegree.put(dependent, inDegree.get(dependent) + 1);
            }
        }
        
        logManager.logDebug("DependencyResolver", "项目入度统计: " + inDegree);
        return inDegree;
    }
    
    /**
     * 将排序结果转换为DatapackInfo列表
     * 
     * @param sortedProjectNames 排序后的项目名称列表
     * @param projectMap 项目映射
     * @return 排序后的数据包列表
     */
    private List<DatapackInfo> convertToDatapackList(List<String> sortedProjectNames, 
                                                    Map<String, DatapackInfo> projectMap) {
        List<DatapackInfo> sortedDatapacks = new ArrayList<>();
        
        for (String projectName : sortedProjectNames) {
            DatapackInfo datapack = projectMap.get(projectName);
            if (datapack != null) {
                sortedDatapacks.add(datapack);
            }
        }
        
        return sortedDatapacks;
    }
    
    /**
     * 分析依赖关系统计信息
     * 
     * @param datapacks 数据包列表
     * @return 统计信息字符串
     */
    public String analyzeDependencyStatistics(List<DatapackInfo> datapacks) {
        if (datapacks == null || datapacks.isEmpty()) {
            return "依赖统计: 无数据包";
        }
        
        int totalHardDeps = 0;
        int totalSoftDeps = 0;
        int projectsWithHardDeps = 0;
        int projectsWithSoftDeps = 0;
        
        for (DatapackInfo datapack : datapacks) {
            int hardDepCount = datapack.getHardDependencies().size();
            int softDepCount = datapack.getSoftDependencies().size();
            
            totalHardDeps += hardDepCount;
            totalSoftDeps += softDepCount;
            
            if (hardDepCount > 0) {
                projectsWithHardDeps++;
            }
            
            if (softDepCount > 0) {
                projectsWithSoftDeps++;
            }
        }
        
        return String.format("依赖统计: 总计 %d 个项目，硬依赖 %d 个（%d 个项目），软依赖 %d 个（%d 个项目）",
                datapacks.size(), totalHardDeps, projectsWithHardDeps, totalSoftDeps, projectsWithSoftDeps);
    }
    
    /**
     * 检查是否存在孤立节点（无依赖且不被依赖的项目）
     * 
     * @param datapacks 数据包列表
     * @return 孤立节点列表
     */
    public List<String> findIsolatedNodes(List<DatapackInfo> datapacks) {
        List<String> isolatedNodes = new ArrayList<>();
        
        Set<String> allProjects = datapacks.stream()
                .map(DatapackInfo::getProjectName)
                .collect(Collectors.toSet());
        
        Set<String> hasDependencies = new HashSet<>();
        Set<String> isDependedUpon = new HashSet<>();
        
        for (DatapackInfo datapack : datapacks) {
            // 检查是否有依赖
            if (!datapack.getHardDependencies().isEmpty() || !datapack.getSoftDependencies().isEmpty()) {
                hasDependencies.add(datapack.getProjectName());
            }
            
            // 检查是否被其他项目依赖
            isDependedUpon.addAll(datapack.getHardDependencies());
            isDependedUpon.addAll(datapack.getSoftDependencies());
        }
        
        for (String project : allProjects) {
            if (!hasDependencies.contains(project) && !isDependedUpon.contains(project)) {
                isolatedNodes.add(project);
            }
        }
        
        return isolatedNodes;
    }
}