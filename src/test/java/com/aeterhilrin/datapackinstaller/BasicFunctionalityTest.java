package com.aeterhilrin.datapackinstaller;

import com.aeterhilrin.datapackinstaller.core.DependencyResolver;
import com.aeterhilrin.datapackinstaller.exception.CyclicDependencyException;
import com.aeterhilrin.datapackinstaller.exception.DependencyResolutionException;
import com.aeterhilrin.datapackinstaller.model.DatapackInfo;
import com.aeterhilrin.datapackinstaller.utils.YamlConfigParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 基础功能测试类
 * 测试核心组件的基本功能
 * 
 * @author AeterHilrin
 */
public class BasicFunctionalityTest {
    
    /**
     * 测试DatapackInfo基本功能
     */
    @Test
    public void testDatapackInfoBasicFunctionality() {
        System.out.println("测试DatapackInfo基本功能...");
        
        // 创建测试数据包
        DatapackInfo datapack = new DatapackInfo("测试项目", "1.0.0", "/test/path", false);
        
        // 测试基本属性
        assertEquals("测试项目", datapack.getProjectName());
        assertEquals("1.0.0", datapack.getVersion());
        assertEquals("测试项目 1.0.0", datapack.getFullName());
        assertEquals("/test/path", datapack.getFilePath());
        assertFalse(datapack.isZipFile());
        
        // 测试依赖解析
        datapack.parseHardDependencies("依赖1 依赖2");
        datapack.parseSoftDependencies("软依赖1");
        
        assertEquals(2, datapack.getHardDependencies().size());
        assertEquals(1, datapack.getSoftDependencies().size());
        assertTrue(datapack.getHardDependencies().contains("依赖1"));
        assertTrue(datapack.getHardDependencies().contains("依赖2"));
        assertTrue(datapack.getSoftDependencies().contains("软依赖1"));
        
        System.out.println("✓ DatapackInfo基本功能测试通过");
    }
    
    /**
     * 测试依赖解析功能
     */
    @Test
    public void testDependencyResolution() {
        System.out.println("测试依赖解析功能...");
        
        DependencyResolver resolver = new DependencyResolver();
        List<DatapackInfo> datapacks = createTestDatapacks();
        
        try {
            List<DatapackInfo> sortedPacks = resolver.resolveDependencies(datapacks);
            
            // 验证排序结果
            assertNotNull(sortedPacks);
            assertEquals(3, sortedPacks.size());
            
            // 验证依赖顺序
            List<String> projectNames = new ArrayList<>();
            for (DatapackInfo pack : sortedPacks) {
                projectNames.add(pack.getProjectName());
            }
            
            // 基础包应该在前面
            int baseIndex = projectNames.indexOf("基础包");
            int extIndex = projectNames.indexOf("扩展包");
            int mainIndex = projectNames.indexOf("主包");
            
            assertTrue("基础包应该在扩展包之前", baseIndex < extIndex);
            assertTrue("扩展包应该在主包之前", extIndex < mainIndex);
            
            System.out.println("✓ 依赖解析功能测试通过");
            
        } catch (DependencyResolutionException | CyclicDependencyException e) {
            fail("依赖解析不应该失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试循环依赖检测
     */
    @Test
    public void testCyclicDependencyDetection() {
        System.out.println("测试循环依赖检测...");
        
        DependencyResolver resolver = new DependencyResolver();
        List<DatapackInfo> cyclicPacks = createCyclicDependencyDatapacks();
        
        try {
            resolver.resolveDependencies(cyclicPacks);
            fail("应该检测到循环依赖");
        } catch (CyclicDependencyException e) {
            System.out.println("✓ 成功检测到循环依赖: " + e.getMessage());
        } catch (DependencyResolutionException e) {
            fail("应该抛出循环依赖异常，而不是依赖解析异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试缺失依赖检测
     */
    @Test
    public void testMissingDependencyDetection() {
        System.out.println("测试缺失依赖检测...");
        
        DependencyResolver resolver = new DependencyResolver();
        List<DatapackInfo> packsWithMissingDep = createMissingDependencyDatapacks();
        
        try {
            resolver.resolveDependencies(packsWithMissingDep);
            fail("应该检测到缺失依赖");
        } catch (DependencyResolutionException e) {
            System.out.println("✓ 成功检测到缺失依赖: " + e.getMessage());
        } catch (CyclicDependencyException e) {
            fail("应该抛出依赖解析异常，而不是循环依赖异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试YAML配置解析
     */
    @Test
    public void testYamlConfigValidation() {
        System.out.println("测试YAML配置验证...");
        
        // 测试有效文件名验证
        assertTrue("正常项目名应该有效", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.isValidFileName("正常项目 1.0.0"));
        
        assertFalse("包含非法字符的名称应该无效", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.isValidFileName("项目<名称"));
        
        assertFalse("空名称应该无效", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.isValidFileName(""));
        
        assertFalse("保留名称应该无效", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.isValidFileName("CON"));
        
        System.out.println("✓ YAML配置验证测试通过");
    }
    
    /**
     * 测试文件扩展名工具
     */
    @Test
    public void testFileExtensionUtils() {
        System.out.println("测试文件扩展名工具...");
        
        assertEquals("zip", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.getFileExtension("test.zip"));
        
        assertEquals("", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.getFileExtension("noextension"));
        
        assertEquals("测试文件", 
                com.aeterhilrin.datapackinstaller.utils.FileUtils.getFileNameWithoutExtension("测试文件.zip"));
        
        System.out.println("✓ 文件扩展名工具测试通过");
    }
    
    /**
     * 创建测试数据包列表（正常依赖关系）
     */
    private List<DatapackInfo> createTestDatapacks() {
        List<DatapackInfo> datapacks = new ArrayList<>();
        
        // 基础包（无依赖）
        DatapackInfo basePack = new DatapackInfo("基础包", "1.0.0", "/test/base", false);
        datapacks.add(basePack);
        
        // 扩展包（依赖基础包）
        DatapackInfo extPack = new DatapackInfo("扩展包", "1.0.0", "/test/ext", false);
        extPack.parseHardDependencies("基础包");
        datapacks.add(extPack);
        
        // 主包（依赖扩展包）
        DatapackInfo mainPack = new DatapackInfo("主包", "1.0.0", "/test/main", false);
        mainPack.parseHardDependencies("扩展包");
        datapacks.add(mainPack);
        
        return datapacks;
    }
    
    /**
     * 创建循环依赖的测试数据包
     */
    private List<DatapackInfo> createCyclicDependencyDatapacks() {
        List<DatapackInfo> datapacks = new ArrayList<>();
        
        // 包A依赖包B
        DatapackInfo packA = new DatapackInfo("包A", "1.0.0", "/test/a", false);
        packA.parseHardDependencies("包B");
        datapacks.add(packA);
        
        // 包B依赖包A（形成循环）
        DatapackInfo packB = new DatapackInfo("包B", "1.0.0", "/test/b", false);
        packB.parseHardDependencies("包A");
        datapacks.add(packB);
        
        return datapacks;
    }
    
    /**
     * 创建缺失依赖的测试数据包
     */
    private List<DatapackInfo> createMissingDependencyDatapacks() {
        List<DatapackInfo> datapacks = new ArrayList<>();
        
        // 依赖不存在的包
        DatapackInfo pack = new DatapackInfo("依赖包", "1.0.0", "/test/dep", false);
        pack.parseHardDependencies("不存在的包");
        datapacks.add(pack);
        
        return datapacks;
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println("=== 开始运行基础功能测试 ===");
        
        BasicFunctionalityTest test = new BasicFunctionalityTest();
        
        try {
            test.testDatapackInfoBasicFunctionality();
            test.testDependencyResolution();
            test.testCyclicDependencyDetection();
            test.testMissingDependencyDetection();
            test.testYamlConfigValidation();
            test.testFileExtensionUtils();
            
            System.out.println("\n=== 所有基础功能测试通过！ ===");
            
        } catch (Exception e) {
            System.err.println("\n=== 测试失败！ ===");
            e.printStackTrace();
        }
    }
    
    /**
     * 主方法，用于独立运行测试
     */
    public static void main(String[] args) {
        runAllTests();
    }
}