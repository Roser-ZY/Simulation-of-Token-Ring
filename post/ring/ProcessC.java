package ring;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ProcessC {
    private static String hostID;                                               // 主机号
    private static String[] hostIDs = new String[8];                            // 存放所有的主机号
    private static Boolean ifReset = false;                                     // 判断环是否处于重置状态，平时为false，当得到reset消息时变为true
    private static Boolean hasData = false;                                     // 判断是否有准备发送的数据，如果有监听线程就要停止转发令牌，并告知进程收到令牌
    private static Boolean isAM = false;                                              // 是否为活动监视站
    //    private static int frameTimes;                                            // 帧经过次数，令牌或者数据，用于监测站
    private static Queue<String> destinationID = new LinkedList<String>();      // 存放目的主机号
    private static Frame receiveData = null;                                    // 接受的数据内容
    private static ServerSocket serverSocket;                                   // 作为前一台主机的Server
    private static Socket clientForProcessSocket;                               // 作为客户Socket向之后的主机发送数据
    private static Socket clientForManagerSocket;                               // 作为客户Socket向Manager发送数据
    private static Socket preSocket;                                            // 前一台主机的Socket
    private static Boolean clientForProcessStatus = false;                      // 判断主机是否连接上后一台主机
    private static Boolean clientForManagerStatus = false;                      // 判断主机是否连接上后一台主机

    // get
    public static String getHostID(){
        return hostID;
    }
    public static String[] getHostIDs(){
        return hostIDs;
    }
    public static Boolean getIfReset(){
        return ifReset;
    }
    public static Boolean ifHasData(){
        return hasData;
    }
    public static Boolean ifIsAM(){
        return isAM;
    }
    public static String getDestination(){
        return destinationID.poll();
    }


    // set
    public static void setHostID(String HostID){
        hostID = HostID;
    }
    public static void setHostIDs(String[] HostIDs){
        hostIDs = HostIDs;
    }
    public static void resetIfReset(){
        ifReset = false;
    }
    public static void copyFrame(Frame frame){
        receiveData = frame;
    }
    public static void flushHasData(){
        if(destinationID.isEmpty()){
            hasData = false;
        }
        else{
            hasData = true;
        }
    }
    public static void addDestination(String destination){
        destinationID.add(destination);
    }
    public static void setIsAM(){
        isAM = true;
    }
    public static void reset(){
        ifReset = true;
        isAM = false;                                           // 提前撤销掉AM身份，这里不会影响此AM删除令牌
    }


    // 与下一台主机连接
    public static void connectNextProcess(int port){
        try {
            clientForProcessSocket = new Socket("localhost", port);
            clientForProcessStatus = true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    // 与Manager连接
    public static void connectManagerProcess(int port){
        try{
            clientForManagerSocket = new Socket("localhost", port);
            clientForManagerStatus = true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 建立服务器
        try {
            serverSocket = new ServerSocket(9003);                                  // 当前主机使用端口9003
        }catch (Exception e){
            e.printStackTrace();
        }

        // 接收前一个主机的Socket
        try {
            preSocket = serverSocket.accept();
            // TODO
            System.out.println("Accepted!");
        }catch (Exception e){
            e.printStackTrace();
        }

        // 建立连接下一台主机的客户端
        while(!clientForProcessStatus) {
            connectNextProcess(9004);                                               // 下一台主机使用端口9004
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        // TODO
        System.out.println("成功连接主机4！");

        // 建立连接Manager的客户端
        while(!clientForManagerStatus){
            connectManagerProcess(10003);                                           // 连接主进程端口10003
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        // TODO
        System.out.println("成功连接Manager！");

        // 创建监听接口
        // 因为作为下一台主机的客户端不需要接收来自下一台主机的消息，所以不需要监听，只需要发送即可
        new Thread(new ProcessCMessageListener(clientForManagerSocket, clientForProcessSocket)).start();            // 监听来自Manager的消息
        new Thread(new ProcessCDataListener(preSocket, clientForProcessSocket, clientForManagerSocket)).start();    // 监听前一台主机发来的数据，第一个为当前主机获取的前一台主机的Socket，第二个为当前主机对下一台主机的客户Socket，第三个为当前主机对主进程的客户Socket

        // 三个线程：产生数据，发送数据，接收数据并发送令牌（除了第一个，其他两个都可以
    }
}
