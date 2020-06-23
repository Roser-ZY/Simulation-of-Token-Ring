package ring;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProcessFrameSender implements Runnable {
    private Socket socket;
    private Frame frame;

    ProcessFrameSender(Socket socket, Frame frame){
        this.socket = socket;
        this.frame = frame;
    }

    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(frame);
            oos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
