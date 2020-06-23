package ring;

import java.net.Socket;

public class ProcessADataSender implements Runnable {
    private Socket nextSocket;                                  // 下一台主机的Socket
    private Socket managerSocket;                               // 主进程Socket
    private String destination;                                 // 目的主机
    private Frame frame = new Frame();                          // 数据帧

    ProcessADataSender(Socket nextSocket, Socket managerSocket){
        this.nextSocket = nextSocket;
        this.managerSocket = managerSocket;
    }

    public void run() {
        // 发送数据前延时1s，能够在图中体现出来
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        // TODO
        System.out.println("Data Send!");
        destination = ProcessA.getDestination();                            // 这里实际上是获取一个目的地址，因为数据内部的东西可以临时生成，我们只需要一个目的地址即可
        ProcessA.flushHasData();                                            // 刷新状态位
        // 初始化数据帧
        frame.isToken = false;                                              // 令牌帧标志位：数据帧
        frame.destinationHost = destination;                                // 目的地址字段
        frame.sourceHost = ProcessA.getHostID();                            // 源地址字段
        frame.content = "Host 1 " + frame.sourceHost + ": Hello World!";    // 数据字段
        frame.isReceived = false;                                           // 接收状态位：未接受
        new Thread(new ProcessFrameSender(nextSocket, frame)).start();      // 向下一台主机发送数据帧
        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_SEND)).start();     // 告知主进程
    }
}
