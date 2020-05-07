package ring;

import java.io.*;
import java.net.ProxySelector;
import java.net.Socket;

public class ProcessAMessageListener implements Runnable {
    private String[] hostIDs = new String[8];                   // 用于读取所有的主机号
    private Socket managerSocket;                               // 监听目标的Socket，这里是主进程的Socket
    private Socket nextSocket;                          // 下一台主机的Socket
    private Message MESSAGE = null;                     // 接收到的消息，消息每接收一次，就置为null，表示无消息

    ProcessAMessageListener(Socket managerSocket, Socket nextSocket){
        this.managerSocket = managerSocket;
        this.nextSocket = nextSocket;
    }

    public void run() {
        while(true) {
            try {
                DataInputStream dis = new DataInputStream(managerSocket.getInputStream());
                // 将Int转换为枚举类型
                MESSAGE = Message.values()[dis.readInt()];
                // TODO
                System.out.println("Has set HostID?" + GlobalStatus.getHasSetHostID());
                System.out.println("Has started?" + GlobalStatus.getHasStarted());
                switch (MESSAGE) {
                    case SET_ID: {
                        if (!GlobalStatus.getHasSetHostID()) {
                            GlobalStatus.setHostID();
                            // 获取所有主机号
                            // 从文件中读取并将读取到的数据写入线程的父进程的数组中
                            try{
                                File hostIDsFile = new File(RingManager.class.getClassLoader().getResource("hostIDs.txt").toURI().getPath());
                                BufferedReader br = new BufferedReader(new FileReader(hostIDsFile));
                                for(int i = 0; i < 8; i++){
                                    hostIDs[i] = br.readLine();
                                }
                                ProcessA.setHostIDs(hostIDs);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            // 设置主机的主机号，通过从文件中读出的数组来设置
                            ProcessA.setHostID(hostIDs[0]);
                            // TODO
                            System.out.println(MESSAGE);
                        }
                    }break;
                    case CHOSEN_AM: {
                        // TODO
                        System.out.println("Host 1: Chosen as AM.");
                        // 如果选为了AM，就将此主机的AM置为true
                        // 没有被选为AM的主机其AM状态位必定为false
                        ProcessA.setIsAM();
                    }break;
                    case START_RING: {
                        if (!GlobalStatus.getHasStarted()) {                                                    // 如果未开始，则开始，如果已开始，则无视
                            GlobalStatus.started();                                                             // 这个要放在这里，因为必须先启动线程才设置状态，如果在线程外，可能设置完成之后线程才进行判断，就会出错
                            // 新建一个生成数据的线程
                            new Thread(new ProcessADataGeneration()).start();
                            // TODO
                            System.out.println("START:" + GlobalStatus.getHasStarted());
                            // 发送令牌
                            // 只有AM才会在一开始发布令牌
                            if(ProcessA.ifIsAM()) {
                                // 延时0.5s再发送，保证通知各进程开始的消息都被接受
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // 通过一个工具类封装一下，更易读
                                Utils.generateToken(managerSocket, nextSocket);
                            }
                        }
                    }break;
                    case RESET_RING: {
                        if (GlobalStatus.getHasStarted()) {
                            // TODO
                            System.out.println("Start:" + GlobalStatus.getHasStarted());
                            // 先将状态位更正
                            // 在主进程中已经保证了不会出现奇奇怪怪的情况，但是这里也是为了保险起见设置了状态位
                            GlobalStatus.reset();
                            // 停止生成数据
                            // 但是这里因为延时的原因，可能我们重置的时候正好此线程在延时过程中，导致结束了还有可能生成一个数据，不过无伤大雅
                            ProcessADataGeneration.setStatus();
                            // TODO
                            System.out.println(MESSAGE);
                            // 如果是AM
                            if(ProcessA.ifIsAM()) {
                                // 设置标志位
                                // 这里将reset标志位置为true，将AM标志位置为false，不会影响，因为已经判断完了，之后删除令牌时只判断reset状态位
                                // 因为只能是AM主机才能将reset置为true
                                ProcessA.reset();
                                // TODO
                                System.out.println("Host 1: Reset " + ProcessA.getIfReset());
                            }
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
