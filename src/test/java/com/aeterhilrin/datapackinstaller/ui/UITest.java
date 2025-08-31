package com.aeterhilrin.datapackinstaller.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * UI测试类
 * 用于测试修复后的UI显示效果
 * 
 * @author AeterHilrin
 */
public class UITest {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            createTestFrame();
        });
    }
    
    private static void createTestFrame() {
        JFrame frame = new JFrame("UI 测试窗口");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 测试协议窗口按钮
        JButton licenseButton = new JButton("测试协议窗口");
        licenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean agreed = LicenseAgreementDialog.showLicenseDialog();
                System.out.println("用户协议结果: " + (agreed ? "同意" : "不同意"));
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(licenseButton, gbc);
        
        // 测试提示窗口按钮
        JButton infoButton = new JButton("测试信息提示");
        infoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageDialog.showInfo("这是一条信息提示消息。\\n" +
                        "测试文字显示是否完整。\\n" +
                        "字体颜色应该是黑色的，不应该是白色。\\n" +
                        "消息窗口应该能完整显示所有文字内容。");
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(infoButton, gbc);
        
        // 测试成功消息
        JButton successButton = new JButton("测试成功消息");
        successButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageDialog.showSuccess("操作成功完成！\\n" +
                        "数据包安装成功。\\n" +
                        "请检查文字显示效果。");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(successButton, gbc);
        
        // 测试警告消息
        JButton warningButton = new JButton("测试警告消息");
        warningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageDialog.showWarning("警告：检测到潜在问题！\\n" +
                        "请确保数据包配置正确。\\n" +
                        "建议备份存档文件。\\n" +
                        "继续操作前请仔细检查。");
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(warningButton, gbc);
        
        // 测试错误消息
        JButton errorButton = new JButton("测试错误消息");
        errorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageDialog.showError("错误：无法完成操作！\\n" +
                        "环境检验不通过，请放置于存档中！\\n" +
                        "请确保：\\n" +
                        "• 软件与datapacks文件夹在同一目录\\n" +
                        "• 软件与level.dat文件在同一目录\\n" +
                        "• 具有必要的文件读写权限");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        frame.add(errorButton, gbc);
        
        // 说明标签
        JLabel instructionLabel = new JLabel("<html><center>" +
                "<b>UI 修复测试</b><br>" +
                "点击按钮测试各种对话框的显示效果<br>" +
                "<i>检查文字是否完整显示，颜色是否正确</i>" +
                "</center></html>");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        frame.add(instructionLabel, gbc);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}