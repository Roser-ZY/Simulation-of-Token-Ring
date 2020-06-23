package ring;

import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;

public class ManagerSender implements Runnable {
    private Socket socket;
    private Message MESSAGE;

    ManagerSender(Socket socket, Message MESSAGE){
        this.socket = socket;
        this.MESSAGE = MESSAGE;
    }

    public void run() {
        try{
            System.out.println("Send");
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(MESSAGE.ordinal());
            dos.flush();
            System.out.println("Have sent.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
