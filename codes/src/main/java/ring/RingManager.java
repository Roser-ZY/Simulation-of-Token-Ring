package ring;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class RingManager {
    private static int chosedHost;                                              // 从0~8，然后通过switch选择发送给谁
    private static String[] hostIDs = {"1", "2", "3", "4", "5", "6", "7", "8"}; // 获取输入的主机号
    private static ServerSocket[] serverSockets = new ServerSocket[8];          // 8个服务器Socket，监听8台主机的消息
    private static Socket[] socket = new Socket[8];                             // 8个服务器Socket获取到的主机Socket

    // UI
    private static JFrame jf;                                                   // JFrame
    // 布局
    private static Box mainLayout;                                              // 主布局，左右两部分
    private static Box leftLayout;                                              // 左布局，包括主机号输入，状态展示以及状态信息
    private static Box rightLayout;                                             // 右布局，包含三个按钮
    private static Box hostIDInputLayout;
    private static Box showLayout;
    private static Box statusLayout;

    // 主机号
    // 标签
    private static JLabel[] hostIDLabel = new JLabel[8];
    // 输入框
    private static JTextField[] hostIDInput = new JTextField[8];
    // 输入Panel
    private static JPanel[] hostIDPanel = new JPanel[8];

    // 展示区域
    private static JLabel statusPicture;

    // 状态报告区域
    private static JTextArea statusReport;
    private static JScrollPane statusJsp;

    // 按钮
    private static JButton setHostIDButton;
    private static JButton startButton;
    private static JButton resetButton;
    // 按钮panel
    private static JPanel setHostIDPanel;
    private static JPanel startPanel;
    private static JPanel resetPanel;

    // get
    public static String[] getHostIDs() {
        return hostIDs;
    }

    // set
    public static void setSetHostIDButton(){
        setHostIDButton.setEnabled(true);
    }
    public static void setStatus(String status){
        statusReport.append("\n" + status + "\n");
        // 设置自动滚动到底部
        try {
            Thread.sleep(50);
        }catch (Exception e){
            e.printStackTrace();
        }
        statusJsp.getVerticalScrollBar().setValue(statusJsp.getVerticalScrollBar().getMaximum());
    }
    public static void setStatusPicture(String name){
        try {
            statusPicture.setIcon(new ImageIcon(RingManager.class.getClassLoader().getResource(name).toURI().getPath()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 创建用户界面
    public static void createUI(){
        // UI
        jf = new JFrame("测试窗口");
        jf.setSize(1600, 900);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 主布局以及左右两个布局
        mainLayout = Box.createHorizontalBox();
        leftLayout = Box.createVerticalBox();
        rightLayout = Box.createVerticalBox();

        // 主机号输入框区域
        hostIDInputLayout = Box.createHorizontalBox();

        // 展示区域
        // 画图
        showLayout = Box.createHorizontalBox();
        // 状态报告区域
        statusLayout = Box.createHorizontalBox();

        // 右侧布局
        // 添加Glue
        rightLayout.add(Box.createVerticalGlue());
        // 添加按钮
        setHostIDButton = new JButton("Set ID");
        setHostIDPanel = new JPanel();
        // 一开始设置为不可用
        setHostIDButton.setEnabled(false);
        setHostIDPanel.add(setHostIDButton);
        rightLayout.add(setHostIDPanel);

        //添加Glue
        rightLayout.add(Box.createVerticalGlue());
        // 添加按钮
        startButton = new JButton("Start");
        startPanel = new JPanel();
        // 一开始设置为不可用
        startButton.setEnabled(false);
        startPanel.add(startButton);
        rightLayout.add(startPanel);

        //添加Glue
        rightLayout.add(Box.createVerticalGlue());
        // 添加按钮
        resetButton = new JButton("Reset");
        resetPanel = new JPanel();
        // 一开始设置为不可用
        resetButton.setEnabled(false);
        resetPanel.add(resetButton);
        rightLayout.add(resetPanel);

        //添加Glue
        rightLayout.add(Box.createVerticalGlue());

        // 为按钮绑定事件
        // 设置主机号
        setHostIDButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 8; i++) {
                    if (!hostIDInput[i].getText().trim().equals("")) {
                        hostIDs[i] = hostIDInput[i].getText().trim();
                    }
                }
                // 将HostIDs写入文件
                try {
                    FileWriter hostIDsWriter = new FileWriter(RingManager.class.getClassLoader().getResource("hostIDs.txt").toURI().getPath());
                    for (int i = 0; i < 8; i++) {
                        hostIDsWriter.write(hostIDs[i] + "\n");
                    }
                    hostIDsWriter.close();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }

                // 通知主机获取自己的主机号
                // TODO
                for (int i = 0; i < 8; i++) {
                    new Thread(new ManagerSender(socket[i], Message.SET_ID)).start();
                }

                // 随机选择主机
                // 此主机也为AM
                // 防止消息发送出错，延时0.2s
                try {
                    Thread.sleep(200);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                Random random = new Random((new Date()).getTime() % 85737);
                chosedHost = random.nextInt(8);
                new Thread(new ManagerSender(socket[chosedHost], Message.CHOSEN_AM)).start();
                System.out.println(chosedHost);
                // 显示信息
                statusReport.append("\nHost " + hostIDs[chosedHost] + " Chosen as AM.\n");
                // 设置自动滚动到底部
                try {
                    Thread.sleep(50);
                }catch (Exception exp){
                    exp.printStackTrace();
                }
                statusJsp.getVerticalScrollBar().setValue(statusJsp.getVerticalScrollBar().getMaximum());

                // 禁用setHostID按钮
                setHostIDButton.setEnabled(false);
                //  启用start按钮
                startButton.setEnabled(true);
            }
        });
        // 开始运行
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //  TODO
                for (int i = 0; i < 8; i++) {
                    new Thread(new ManagerSender(socket[i], Message.START_RING)).start();
                }

                // 禁用start按钮
                startButton.setEnabled(false);
                // 启用reset按钮
                resetButton.setEnabled(true);
            }
        });
        // 重置
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO
                for(int i = 0; i < 8; i++) {
                    new Thread(new ManagerSender(socket[i], Message.RESET_RING)).start();
                }

                // 禁用reset按钮
                resetButton.setEnabled(false);
            }
        });

        // 主机号
        for(int i = 0; i < 8; i++){
            hostIDLabel[i] = new JLabel();
            hostIDLabel[i].setText("Host" + " " + (i+1) + ":");
            hostIDInput[i] = new JTextField(8);
            hostIDPanel[i] = new JPanel();
            hostIDPanel[i].add(hostIDLabel[i]);
            hostIDPanel[i].add(hostIDInput[i]);
            hostIDInputLayout.add(hostIDPanel[i]);
            hostIDInputLayout.add(Box.createHorizontalGlue());
        }

        // 将主机号输入布局添加到左边布局
        leftLayout.add(hostIDInputLayout);

        // 展示区域
        statusPicture = new JLabel();
        setStatusPicture("initial.png");
        showLayout.add(statusPicture);
        leftLayout.add(showLayout);
        leftLayout.add(Box.createVerticalGlue());

        // 状态报告
        statusReport = new JTextArea();
        statusJsp = new JScrollPane(statusReport);
        statusReport.append("Status Report:\n");
        statusReport.setEditable(false);            // 设置不可编辑
        statusLayout.add(statusJsp);
        // 将状态报告布局添加到左侧布局
        leftLayout.add(statusLayout);

        // 主布局
        mainLayout.add(leftLayout);
        mainLayout.add(rightLayout);

        jf.setContentPane(mainLayout);
        jf.setVisible(true);
    }

    public static void main(String[] args) {
        // 创建UI
        createUI();

        try{
            // 初始化
            for(int i = 0; i < 8; i++){
                serverSockets[i] = new ServerSocket(10001+i);
            }

            // TODO 优化
            // 打开8台主机，通过进程实现
            new Thread(new StartHosts()).start();

            // 阻塞，这里只有都获取到才能够进行令牌环的模拟
            // 为了简单我们没有进行是否重连等判断，也没有构建长连接，只连接一次
            for(int i = 0; i < 8; i++) {
                socket[i] = serverSockets[i].accept();
                // 使用线程
                // 还要传送监听的是哪一台主机
                new Thread(new ManagerListener(socket[i], i+1)).start();
            }

            // 返回就绪消息，表示此时可以进行操作
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            statusReport.append("\n\nHosts initialized!\n");

            // 初始化完成后启用set HostID按钮
            setHostIDButton.setEnabled(true);

        }catch (IOException ioe){
            System.out.println("There is an error while creating connection.");
        }
    }
}
