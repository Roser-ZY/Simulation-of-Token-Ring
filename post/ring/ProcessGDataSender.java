package ring;

import java.net.Socket;

public class ProcessGDataSender implements Runnable {
    private Socket nextSocket;                                  // 下一台主机的Socket
    private Socket managerSocket;                               // 主进程Socket
    private String destination;                                 // 目的主机
    private Frame frame = new Frame();                          // 数据帧

    ProcessGDataSender(Socket nextSocket, Socket managerSocket){
        this.nextSocket = nextSocket;
        this.managerSocket = managerSocket;
    }

    public void run() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Data Send!");
        // TODO
        destination = ProcessG.getDestination();                            // 这里实际上是获取一个目的地址，因为数据内部的东西可以临时生成，我们只需要一个目的地址即可
        ProcessG.flushHasData();                                            // 刷新状态位
        // 初始化数据帧
        frame.isToken = false;
        frame.destinationHost = destination;
        // TODO 修改Hosts
        frame.sourceHost = ProcessG.getHostID();
        frame.content = "Host 7 " + frame.sourceHost + ": Hello World!";
        frame.isReceived = false;
        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_SEND)).start();
    }
}
