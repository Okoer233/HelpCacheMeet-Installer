package com.aeterhilrin.datapackinstaller.ui;

import com.aeterhilrin.datapackinstaller.logging.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * 软件使用协议确认对话框
 * 用户必须同意协议才能继续使用软件
 * 
 * @author AeterHilrin
 */
public class LicenseAgreementDialog extends JDialog {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 对话框结果 */
    private boolean agreed = false;
    
    /** 协议文本内容 */
    private static final String LICENSE_TEXT = 
            "软件使用协议\n\n" +
            "本软件基于MIT协议开源，您可以自由使用、修改和分发本软件。\n\n" +
            "注意！您不能宣称该软件是您本人开发！\n\n" +
            "MIT协议条款：\n" +
            "• 您可以自由使用本软件用于任何目的\n" +
            "• 您可以修改和分发本软件的副本\n" +
            "• 您必须在所有副本中保留版权声明和许可声明\n" +
            "• 本软件按\"现状\"提供，不提供任何形式的保证\n" +
            "• 作者不承担因使用本软件而产生的任何责任\n\n" +
            "使用条件：\n" +
            "• 本软件仅用于Minecraft数据包的安装和管理\n" +
            "• 请确保在使用前备份您的存档文件\n" +
            "• 作者不对数据丢失或存档损坏承担责任\n\n" +
            "技术说明：\n" +
            "• 本软件会修改您的level.dat文件\n" +
            "• 本软件会重命名datapacks文件夹中的数据包\n" +
            "• 本软件会在存档目录下创建日志文件夹\n\n" +
            "——By AeterHilrin";
    
    /**
     * 构造函数
     * 
     * @param parent 父窗口
     */
    public LicenseAgreementDialog(Window parent) {
        super(parent, "软件使用协议", ModalityType.APPLICATION_MODAL);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
    }
    
    /**
     * 显示协议对话框
     * 
     * @return 用户是否同意协议
     */
    public boolean showDialog() {
        logManager.logInfo("LicenseAgreementDialog", "显示软件使用协议对话框");
        
        setVisible(true);
        
        logManager.logInfo("LicenseAgreementDialog", 
                "用户协议确认结果: " + (agreed ? "同意" : "不同意"));
        
        return agreed;
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 设置窗口图标
        try {
            setIconImage(createDefaultIcon());
        } catch (Exception e) {
            logManager.logWarn("LicenseAgreementDialog", "设置窗口图标失败", e);
        }
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // 创建协议文本面板
        JScrollPane textPanel = createTextPanel();
        add(textPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加边距
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    /**
     * 创建标题面板
     * 
     * @return 标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("软件使用协议", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK); // 修复为黑色
        
        JLabel subtitleLabel = new JLabel("请仔细阅读以下协议条款", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(80, 80, 80)); // 改为更深的灰色
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        return titlePanel;
    }
    
    /**
     * 创建协议文本面板
     * 
     * @return 文本面板
     */
    private JScrollPane createTextPanel() {
        JTextArea textArea = new JTextArea(LICENSE_TEXT);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        textArea.setBackground(new Color(248, 248, 248));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 修复文本区域的颜色，确保文字是黑色
        textArea.setForeground(Color.BLACK); // 改为纯黑色
        textArea.setSelectionColor(new Color(184, 207, 229));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        return scrollPane;
    }
    
    /**
     * 创建按钮面板
     * 
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // 同意按钮（左侧）
        JButton agreeButton = new JButton("同意");
        agreeButton.setPreferredSize(new Dimension(100, 35));
        agreeButton.setForeground(Color.BLACK); // 设置文字为黑色
        agreeButton.setBackground(Color.WHITE); // 不给按钮上色
        agreeButton.setFocusPainted(false);
        agreeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // 不同意按钮（右侧）
        JButton disagreeButton = new JButton("不同意");
        disagreeButton.setPreferredSize(new Dimension(100, 35));
        disagreeButton.setForeground(Color.BLACK); // 设置文字为黑色
        disagreeButton.setBackground(Color.WHITE); // 不给按钮上色
        disagreeButton.setFocusPainted(false);
        disagreeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // 按照要求布局：左边是同意，右边是不同意
        buttonPanel.add(agreeButton);
        buttonPanel.add(disagreeButton);
        
        return buttonPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 获取按钮（按照新的布局顺序：同意在左，不同意在右）
        JPanel buttonPanel = (JPanel) ((JPanel) getContentPane().getComponent(2));
        JButton agreeButton = (JButton) buttonPanel.getComponent(0); // 第一个按钮是同意
        JButton disagreeButton = (JButton) buttonPanel.getComponent(1); // 第二个按钮是不同意
        
        // 同意按钮事件
        agreeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAgreeClicked();
            }
        });
        
        // 不同意按钮事件
        disagreeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDisagreeClicked();
            }
        });
        
        // 窗口关闭事件（等同于不同意）
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                onDisagreeClicked();
            }
        });
        
        // ESC键关闭窗口
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDisagreeClicked();
            }
        });
    }
    
    /**
     * 配置对话框属性
     */
    private void configureDialog() {
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
        
        // 设置最小尺寸
        setMinimumSize(getSize());
    }
    
    /**
     * 处理同意按钮点击事件
     */
    private void onAgreeClicked() {
        logManager.logInfo("LicenseAgreementDialog", "用户点击同意按钮");
        agreed = true;
        dispose();
    }
    
    /**
     * 处理不同意按钮点击事件
     */
    private void onDisagreeClicked() {
        logManager.logInfo("LicenseAgreementDialog", "用户点击不同意按钮或关闭窗口");
        agreed = false;
        dispose();
    }
    
    /**
     * 创建默认图标
     * 
     * @return 默认图标
     */
    private Image createDefaultIcon() {
        // 创建一个简单的16x16图标
        Image icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) icon.getGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制背景
        g2d.setColor(new Color(76, 175, 80));
        g2d.fillRoundRect(0, 0, 16, 16, 4, 4);
        
        // 绘制字母"H"
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (16 - fm.stringWidth("H")) / 2;
        int y = (16 - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString("H", x, y);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * 静态方法：显示协议对话框
     * 
     * @param parent 父窗口
     * @return 用户是否同意协议
     */
    public static boolean showLicenseDialog(Window parent) {
        LicenseAgreementDialog dialog = new LicenseAgreementDialog(parent);
        return dialog.showDialog();
    }
    
    /**
     * 静态方法：显示协议对话框（无父窗口）
     * 
     * @return 用户是否同意协议
     */
    public static boolean showLicenseDialog() {
        return showLicenseDialog(null);
    }
}