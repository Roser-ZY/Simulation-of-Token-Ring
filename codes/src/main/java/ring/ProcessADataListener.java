package ring;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

public class ProcessADataListener implements Runnable {
    private Socket socket;              // 被监听的主机的客户端Socket（即前一台主机的Socket）
    private Socket nextSocket;          // 下一台主机的服务器socket
    private Socket managerSocket;       // 主进程，用于发送信息给主进程
    private Frame frame;                // 收到的数据帧
    // 模拟未接收的情况
    private Random random = new Random(((new Date()).getTime() % 2846));

    ProcessADataListener(Socket socket, Socket nextSocket, Socket managerSocket){
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
                // 每次监听到都延时1s，在这里延时很方便，不管数据还是令牌都延时1s
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                // 如果收到的帧帧为令牌帧
                if(frame.isToken){
                    // TODO
                    System.out.println("Host1: Get Token!");
                    // 判断是否reset，这个标志位只有当主机为AM时才会被设置
                    if(ProcessA.getIfReset()){
                        // TODO
                        System.out.println("Reset!");
                        // 重置状态位，重置为false，防止下次开始时又重置整个环
                        ProcessA.resetIfReset();
                        // 通知主进程
                        // 主要是为了更换图片和打印状态信息
                        new Thread(new ProcessMessageSender(managerSocket, Message.RESET_COMPLETE)).start();
                    }
                    // 如果有数据，则将令牌截获，并发送数据
                    else if(ProcessA.ifHasData()){
                        // 向主进程发送消息
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_RECEIVED)).start();
                        // 创建发送数据的线程
                        // 这一步就是发送数据
                        new Thread(new ProcessADataSender(nextSocket, managerSocket)).start();
                    }
                    // 否则将令牌转发
                    else{
                        // 向主进程发送消息
                        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_ARRIVED)).start();
                        // 转发令牌帧
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                }
                else{
                    // 如果到达源主机，则判断是否已被接收
                    if(frame.sourceHost.equals(ProcessA.getHostID())){
                        // 如果已经被接收
                        if(frame.isReceived){
                            // TODO
                            System.out.println("Data arrived destination correctly!");
                            // 告知主进程
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_Y)).start();
                        }
                        else{
                            // TODO
                            System.out.println("Data arrived destination incorrectly!");
                            // 告知主进程
                            new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED_SOURCE_N)).start();
                        }
                        // 生成新令牌
                        // 不管是否接收都不管了，直接舍弃，发送新的令牌
                        // 这里要扩展也很容易，不过好像令牌环的出错率很低，所以都是不可靠传输
                        Utils.generateToken(managerSocket, nextSocket);
                    }
                    // 如果到达目的主机，则接收
                    // 可以在此调整出错的概率，这里是2%的出错率，当然可以随便调整，如果想要展示效果的话，可以调高一些
                    // TODO 将概率更改回去
                    else if(frame.destinationHost.equals(ProcessA.getHostID()) && random.nextInt(50) == 23){
                        // 更改状态位
                        frame.isReceived = true;
                        // 接收数据帧
                        // 这一步是模拟复制过程，但是这一步在这个程序中没有什么用处，仅仅是为了模拟复制过程
                        ProcessA.copyFrame(frame);
                        // 告知主进程
                        // 调用线程显示数据内容
                        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_RECEIVED)).start();
                        new Thread(new ProcessDataReceiver(frame)).start();
                        // 继续转发
                        // 接收之后要继续转发
                        new Thread(new ProcessFrameSender(nextSocket, frame)).start();
                    }
                    // 如果没有到源主机或目的主机，则将数据转发
                    else{
                        // 告知主进程
                        new Thread(new ProcessMessageSender(managerSocket, Message.DATA_ARRIVED)).start();
                        // 转发帧
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
