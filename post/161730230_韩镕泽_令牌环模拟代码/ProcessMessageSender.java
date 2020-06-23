package ring;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ProcessMessageSender implements Runnable {
    private Socket socket;
    private Message MESSAGE;

    ProcessMessageSender(Socket socket, Message MESSAGE){
        this.socket = socket;
        this.MESSAGE = MESSAGE;
    }

    public void run() {
        try{
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            // 通过将枚举类型转换为Int发送
            dos.writeInt(MESSAGE.ordinal());
            dos.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
