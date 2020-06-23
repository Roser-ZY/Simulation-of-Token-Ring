package ring;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

public class ProcessFDataListener implements Runnable {
    private Socket socket;              // 被监听的主机的客户端Socket
    private Socket nextSocket;          // 下一台主机的服务器socket
    private Socket managerSocket;       // 主进程，用于发送信息给主进程
    private Frame frame;                // 收到的数据帧
    // 模拟未接收的情况
    private Random random = new Random(((new Date()).getTime() % 2732));

    ProcessFDataListener(Socket socket, Socket nextSocket, Socket managerSocket){
        this.socket = socket;
        this.nextSocket = nextSocket;
        this.managerSocket = managerSocket;
    }
    public void run() {
        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                // 如果没有数据这里就会阻塞，所以不必担心会重复执行后面的操作
                frame = (Frame)ois.readObject();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(frame.isToken){
                    // TODO
                    System.out.println("Host5: Get Token!");
                    // 判断是否reset
                    if(ProcessF.getIfReset()){
                        // TODO
                        System.out.println("Reset!");
                        // 重置状态位
                        ProcessF.resetIfReset();
                        // 通知主进程
                        new Thread(new ProcessMessageSender(managerSocket, Message.RESET_COMPLETE)).start();
                    }
                    // 如果有数据，则将令牌截获，并发送数据
                    else if(ProcessF.ifHasData()){
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_RECEIVED)).start();
                        // 创建发送数据的线程
                        new Thread(new ProcessFDataSender(nextSocket, managerSocket)).start();
                    }
                    // 否则将令牌转发
                    else{
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_ARRIVED)).start();
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                }
                else{
                    // 如果到达源主机，则判断是否已被接受
                    if(frame.sourceHost.equals(ProcessF.getHostID())){
                        if(frame.isReceived){
                            // TODO
                            System.out.println("Data arrived destination correctly!");
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_Y)).start();
                        }
                        else{
                            // TODO
                            System.out.println("Data arrived destination incorrectly!");
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_N)).start();
                        }
                        // 生成新令牌
                        Utils.generateToken(managerSocket, nextSocket);
                    }
                    // 如果到达目的主机，则接收
                    else if(frame.destinationHost.equals(ProcessF.getHostID()) && random.nextInt(50) != 34){
                        frame.isReceived = true;
                        ProcessF.copyFrame(frame);
                        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_RECEIVED)).start();
                        // 接收数据帧
                        new Thread(new ProcessDataReceiver(frame)).start();
                        // 继续转发
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                    // 否则将数据转发
                    else{
                        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED)).start();
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
    }
}
