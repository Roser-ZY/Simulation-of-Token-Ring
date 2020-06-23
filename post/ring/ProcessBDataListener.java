package ring;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

public class ProcessBDataListener implements Runnable {
    private Socket socket;              // 被监听的主机的客户端Socket
    private Socket nextSocket;          // 下一台主机的服务器socket
    private Socket managerSocket;       // 主进程，用于发送信息给主进程
    private Frame frame;                // 收到的数据帧
    // 模拟未接收的情况
    private Random random = new Random(((new Date()).getTime() % 3384));

    ProcessBDataListener(Socket socket, Socket nextSocket, Socket managerSocket){
        this.socket = socket;
        this.nextSocket = nextSocket;
        this.managerSocket = managerSocket;
    }

    public void run() {
        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                frame = (Frame)ois.readObject();
                // 接收再发送，间隔均为1s（当然是为了方便，直接在这里延时即可）
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(frame.isToken){
                    // TODO
                    System.out.println("Host2: Get Token!");
                    // 判断是否reset
                    if(ProcessB.getIfReset()){
                        // TODO
                        System.out.println("Reset!");
                        // 重置状态位
                        ProcessB.resetIfReset();
                        // 通知主进程
                        new Thread(new ProcessMessageSender(managerSocket, Message.RESET_COMPLETE)).start();
                    }
                    // 如果有数据，则将令牌截获，并发送数据
                    else if(ProcessB.ifHasData()){
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_RECEIVED)).start();
                        new Thread(new ProcessBDataSender(nextSocket, managerSocket)).start();
                    }
                    // 否则将令牌转发
                    else{
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_ARRIVED)).start();
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                }
                else{
                    // 如果到达源主机，则判断是否已被接受
                    if(frame.sourceHost.equals(ProcessB.getHostID())){
                        if(frame.isReceived){
                            // TODO
                            System.out.println("Data arrived destination correctly!");
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_Y)).start();
                            // 生成新令牌
                            Utils.generateToken(managerSocket, nextSocket);
                        }
                        else{
                            // TODO
                            System.out.println("Data arrived destination incorrectly!");
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_N)).start();
                            // 生成新令牌
                            Utils.generateToken(managerSocket, nextSocket);
                        }
                    }
                    // 如果到达目的主机，则接收
                    // TODO 将概率更改回去
                    else if(frame.destinationHost.equals(ProcessB.getHostID()) && random.nextInt(50) == 36){
                        frame.isReceived = true;
                        // 接收数据帧
                        ProcessB.copyFrame(frame);
                        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_RECEIVED)).start();
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
