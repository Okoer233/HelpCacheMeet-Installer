package com.aeterhilrin.datapackinstaller.ui;

import com.aeterhilrin.datapackinstaller.logging.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * 消息提示对话框
 * 用于显示各种提示信息给用户
 * 
 * @author AeterHilrin
 */
public class MessageDialog extends JDialog {
    
    private static final LogManager logManager = LogManager.getInstance();
    
    /** 消息类型枚举 */
    public enum MessageType {
        INFO("提示", new Color(33, 150, 243)),      // 蓝色
        SUCCESS("成功", new Color(76, 175, 80)),    // 绿色
        WARNING("警告", new Color(255, 152, 0)),    // 橙色
        ERROR("错误", new Color(244, 67, 54));      // 红色
        
        private final String title;
        private final Color color;
        
        MessageType(String title, Color color) {
            this.title = title;
            this.color = color;
        }
        
        public String getTitle() {
            return title;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param parent 父窗口
     * @param messageType 消息类型
     * @param message 消息内容
     */
    public MessageDialog(Window parent, MessageType messageType, String message) {
        super(parent, messageType.getTitle(), ModalityType.APPLICATION_MODAL);
        initializeDialog(messageType, message);
    }
    
    /**
     * 初始化对话框
     * 
     * @param messageType 消息类型
     * @param message 消息内容
     */
    private void initializeDialog(MessageType messageType, String message) {
        setupComponents(messageType, message);
        configureDialog();
        logManager.logInfo("MessageDialog", 
                String.format("显示%s消息: %s", messageType.getTitle(), message));
    }
    
    /**
     * 设置组件
     * 
     * @param messageType 消息类型
     * @param message 消息内容
     */
    private void setupComponents(MessageType messageType, String message) {
        setLayout(new BorderLayout(15, 15));
        
        // 创建图标面板
        JPanel iconPanel = createIconPanel(messageType);
        add(iconPanel, BorderLayout.WEST);
        
        // 创建消息面板
        JPanel messagePanel = createMessagePanel(message);
        add(messagePanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加边距
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 设置窗口图标
        try {
            setIconImage(createMessageIcon(messageType));
        } catch (Exception e) {
            logManager.logWarn("MessageDialog", "设置窗口图标失败", e);
        }
    }
    
    /**
     * 创建图标面板
     * 
     * @param messageType 消息类型
     * @return 图标面板
     */
    private JPanel createIconPanel(MessageType messageType) {
        JPanel iconPanel = new JPanel(new BorderLayout());
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(createMessageIcon(messageType, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        iconPanel.setPreferredSize(new Dimension(60, 60));
        
        return iconPanel;
    }
    
    /**
     * 创建消息面板
     * 
     * @param message 消息内容
     * @return 消息面板
     */
    private JPanel createMessagePanel(String message) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setOpaque(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
        messageArea.setForeground(Color.BLACK); // 修复字体颜色为黑色
        messageArea.setBackground(Color.WHITE); // 设置背景为白色
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 改进尺寸计算，确保文字显示完整
        FontMetrics fm = messageArea.getFontMetrics(messageArea.getFont());
        String[] lines = message.split("\\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        
        // 计算合适的宽度和高度
        int preferredWidth = Math.min(500, Math.max(300, maxWidth + 40));
        int lineCount = Math.max(lines.length, (message.length() / 40) + 1);
        int preferredHeight = Math.max(80, lineCount * (fm.getHeight() + 2) + 40);
        
        messageArea.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        
        // 使用滚动面板以防止文字过长
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        messagePanel.add(scrollPane, BorderLayout.CENTER);
        
        return messagePanel;
    }
    
    /**
     * 创建按钮面板
     * 
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton confirmButton = new JButton("确定");
        confirmButton.setPreferredSize(new Dimension(80, 30));
        confirmButton.setBackground(Color.WHITE); // 按钮背景为白色
        confirmButton.setForeground(Color.BLACK); // 按钮文字为黑色
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 设置确定按钮为默认按钮
        getRootPane().setDefaultButton(confirmButton);
        
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConfirmClicked();
            }
        });
        
        buttonPanel.add(confirmButton);
        
        return buttonPanel;
    }
    
    /**
     * 配置对话框属性
     */
    private void configureDialog() {
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        
        // 设置ESC键关闭窗口
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 设置Enter键确认
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke("ENTER");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke, "ENTER");
        getRootPane().getActionMap().put("ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConfirmClicked();
            }
        });
    }
    
    /**
     * 处理确定按钮点击事件
     */
    private void onConfirmClicked() {
        dispose();
    }
    
    /**
     * 显示对话框
     */
    public void showDialog() {
        setVisible(true);
    }
    
    /**
     * 创建消息图标
     * 
     * @param messageType 消息类型
     * @param size 图标大小
     * @return 图标
     */
    private ImageIcon createMessageIcon(MessageType messageType, int size) {
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) icon.getGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制背景圆圈
        g2d.setColor(messageType.getColor());
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        // 绘制边框
        g2d.setColor(messageType.getColor().darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(2, 2, size - 4, size - 4);
        
        // 绘制图标符号
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 3));
        
        String symbol;
        switch (messageType) {
            case INFO:
                symbol = "i";
                break;
            case SUCCESS:
                symbol = "✓";
                break;
            case WARNING:
                symbol = "!";
                break;
            case ERROR:
                symbol = "✗";
                break;
            default:
                symbol = "?";
                break;
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(symbol)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(symbol, x, y);
        
        g2d.dispose();
        return new ImageIcon(icon);
    }
    
    /**
     * 创建消息图标（16x16，用于窗口图标）
     * 
     * @param messageType 消息类型
     * @return 图标
     */
    private Image createMessageIcon(MessageType messageType) {
        return createMessageIcon(messageType, 16).getImage();
    }
    
    // 静态方法：便捷的消息显示
    
    /**
     * 显示信息消息
     * 
     * @param parent 父窗口
     * @param message 消息内容
     */
    public static void showInfo(Window parent, String message) {
        MessageDialog dialog = new MessageDialog(parent, MessageType.INFO, message);
        dialog.showDialog();
    }
    
    /**
     * 显示成功消息
     * 
     * @param parent 父窗口
     * @param message 消息内容
     */
    public static void showSuccess(Window parent, String message) {
        MessageDialog dialog = new MessageDialog(parent, MessageType.SUCCESS, message);
        dialog.showDialog();
    }
    
    /**
     * 显示警告消息
     * 
     * @param parent 父窗口
     * @param message 消息内容
     */
    public static void showWarning(Window parent, String message) {
        MessageDialog dialog = new MessageDialog(parent, MessageType.WARNING, message);
        dialog.showDialog();
    }
    
    /**
     * 显示错误消息
     * 
     * @param parent 父窗口
     * @param message 消息内容
     */
    public static void showError(Window parent, String message) {
        MessageDialog dialog = new MessageDialog(parent, MessageType.ERROR, message);
        dialog.showDialog();
    }
    
    // 无父窗口的便捷方法
    
    /**
     * 显示信息消息（无父窗口）
     * 
     * @param message 消息内容
     */
    public static void showInfo(String message) {
        showInfo(null, message);
    }
    
    /**
     * 显示成功消息（无父窗口）
     * 
     * @param message 消息内容
     */
    public static void showSuccess(String message) {
        showSuccess(null, message);
    }
    
    /**
     * 显示警告消息（无父窗口）
     * 
     * @param message 消息内容
     */
    public static void showWarning(String message) {
        showWarning(null, message);
    }
    
    /**
     * 显示错误消息（无父窗口）
     * 
     * @param message 消息内容
     */
    public static void showError(String message) {
        showError(null, message);
    }
}