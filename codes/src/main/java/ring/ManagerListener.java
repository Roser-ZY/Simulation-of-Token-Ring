package ring;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Date;

public class ManagerListener implements Runnable {
    private int listenHost;                         // 标识监听的主机
    // 存放显示data传送的图片名
    private String[] pictureNames_D = {"data-I.png", "data-II.png", "data-III.png", "data-IV.png", "data-V.png", "data-VI.png", "data-VII.png", "data-VIII.png"};
    // 存放显示token传送的图片名
    private String[] pictureNames_T = {"token-I.png", "token-II.png", "token-III.png", "token-IV.png", "token-V.png", "token-VI.png", "token-VII.png", "token-VIII.png"};
    // 存放数据被正确接收的图片名
    private String[] pictureNames_RY = {"data-Y-I.png", "data-Y-II.png", "data-Y-III.png", "data-Y-IV.png", "data-Y-V.png", "data-Y-VI.png", "data-Y-VII.png", "data-Y-VIII.png"};
    // 存放数据未被正确接收的图片名
    private String[] pictureNames_RN = {"data-N-I.png", "data-N-II.png", "data-N-III.png", "data-N-IV.png", "data-N-V.png", "data-N-VI.png", "data-N-VII.png", "data-N-VIII.png"};
    private Socket socket;                          // 监听主机的Socket
    private Message MESSAGE;

    ManagerListener(Socket socket, int listenHost){
        this.socket = socket;
        this.listenHost = listenHost;
    }

    public void run() {
        while(true){
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                // 要将int转换为枚举类型
                MESSAGE = Message.values()[dis.readInt()];
                System.out.println(MESSAGE);
                switch (MESSAGE){
                    case TOKEN_SEND:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Create and Send a Token.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_T[listenHost-1]);
                    }break;
                    case TOKEN_ARRIVED:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Token arrived.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_T[listenHost-1]);
                    }break;
                    case TOKEN_RECEIVED:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Token received.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_T[listenHost-1]);
                    }break;
                    case DATA_SEND:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Send a Data Frame.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_D[listenHost-1]);
                    }break;
                    case DATA_ARRIVED:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Data Frame arrived.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_D[listenHost-1]);
                    }break;
                    case DATA_RECEIVED:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Data Frame received.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_D[listenHost-1]);
                    }break;
                    case DATA_ARRIVED_SOURCE_Y:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Data is received by destination host.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_RY[listenHost-1]);
                    }break;
                    case DATA_ARRIVED_SOURCE_N:{
                        RingManager.setStatus("\n" + (new Date()).toString() + "\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Data is not received by destination host.\n");
                        // 显示图片
                        RingManager.setStatusPicture(pictureNames_RN[listenHost-1]);
                    }break;
                    case RESET_COMPLETE:{
                        RingManager.setStatus("\nHost" + RingManager.getHostIDs()[listenHost-1] + ": Reset.\n");
                        // 启用setHostID按钮
                        RingManager.setSetHostIDButton();
                        // 显示图片
                        RingManager.setStatusPicture("initial.png");
                    }break;
                    default:break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
