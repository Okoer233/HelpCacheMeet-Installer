package com.aeterhilrin.datapackinstaller;

/**
 * 简单的编译测试类
 * 用于验证基本的代码编译是否正常
 * 
 * @author AeterHilrin
 */
public class SimpleCompileTest {
    
    /**
     * 测试基本功能
     */
    public static void testBasicFunctionality() {
        System.out.println("=== 简单编译测试 ===");
        
        try {
            // 测试基本的类加载
            Class<?> mainClass = Class.forName("com.aeterhilrin.datapackinstaller.Main");
            System.out.println("✓ Main 类加载成功");
            
            Class<?> controllerClass = Class.forName("com.aeterhilrin.datapackinstaller.core.MainController");
            System.out.println("✓ MainController 类加载成功");
            
            Class<?> logManagerClass = Class.forName("com.aeterhilrin.datapackinstaller.logging.LogManager");
            System.out.println("✓ LogManager 类加载成功");
            
            Class<?> datapackInfoClass = Class.forName("com.aeterhilrin.datapackinstaller.model.DatapackInfo");
            System.out.println("✓ DatapackInfo 类加载成功");
            
            System.out.println("\n=== 编译测试通过！ ===");
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ 类加载失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 主方法，用于独立运行测试
     */
    public static void main(String[] args) {
        testBasicFunctionality();
    }
}