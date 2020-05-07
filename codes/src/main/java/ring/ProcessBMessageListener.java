package ring;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;

public class ProcessBMessageListener implements Runnable{
    private String[] hostIDs = new String[8];                   // 用于读取所有的主机号
    private Socket managerSocket;                              // 监听目标的Socket，这里是主进程的Socket
    private Socket nextSocket;                          // 下一台主机的Socket
    private Message MESSAGE = null;                     // 接收到的消息，消息每接收一次，就置为null，表示无消息

    ProcessBMessageListener(Socket managerSocket, Socket nextSocket){
        this.managerSocket = managerSocket;
        this.nextSocket = nextSocket;
    }

    public void run() {
        while(true) {
            try {
                DataInputStream dis = new DataInputStream(managerSocket.getInputStream());
                // 将Int转换为枚举类型
                MESSAGE = Message.values()[dis.readInt()];
                System.out.println("Has set HostID?" + GlobalStatus.getHasSetHostID());
                System.out.println("Has started?" + GlobalStatus.getHasStarted());
                switch (MESSAGE) {
                    // TODO 更改ID
                    case SET_ID: {
                        if (!GlobalStatus.getHasSetHostID()) {
                            GlobalStatus.setHostID();
                            // 获取主机号
                            try{
                                File hostIDsFile = new File(RingManager.class.getClassLoader().getResource("hostIDs.txt").toURI().getPath());
                                BufferedReader br = new BufferedReader(new FileReader(hostIDsFile));
                                for(int i = 0; i < 8; i++){
                                    hostIDs[i] = br.readLine();
                                }
                                ProcessB.setHostIDs(hostIDs);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            ProcessB.setHostID(hostIDs[1]);
                            System.out.println(MESSAGE);
                        }
                        break;
                    }
                    case CHOSEN_AM: {
                        System.out.println("Host 2: Chosen as AM.");
                        ProcessB.setIsAM();
                        break;
                    }
                    // TODO
                    case START_RING: {
                        if (!GlobalStatus.getHasStarted()) {                                                    // 如果未开始，则开始，如果已开始，则无视
                            GlobalStatus.started();                                                             // 这个要放在这里，因为必须先启动线程才设置状态，如果在线程外，可能设置完成之后线程才进行判断，就会出错
                            // 新建一个生成数据的线程
                            new Thread(new ProcessBDataGeneration()).start();
                            // 发送令牌
                            if(ProcessB.ifIsAM()) {
                                // 延时0.5s再发送，保证开始进程的通知完成
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Utils.generateToken(managerSocket, nextSocket);
                            }
                        }
                    }break;
                    case RESET_RING: {
                        if (GlobalStatus.getHasStarted()) {
                            System.out.println("Start:" + GlobalStatus.getHasStarted());
                            GlobalStatus.reset();
                            // 停止生成数据的线程
                            ProcessBDataGeneration.setStatus();
                            System.out.println(MESSAGE);
                            if(ProcessB.ifIsAM()) {
                                // 设置标志位
                                ProcessB.reset();
                                System.out.println("Host 2: Reset" + ProcessB.getIfReset());
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
