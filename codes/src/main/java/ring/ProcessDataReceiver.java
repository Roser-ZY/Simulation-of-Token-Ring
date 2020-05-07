package ring;

import java.net.Socket;

public class ProcessDataReceiver implements Runnable {
    private Frame frame;

    ProcessDataReceiver(Frame frame){
        this.frame = frame;
    }

    public void run() {
        System.out.println(frame.content);
    }
}
