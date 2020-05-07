package demo;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSent implements Runnable{
    private Socket socket;

    ServerSent(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try{
            while(true) {
                PrintWriter pr = new PrintWriter(socket.getOutputStream());
                Thread.sleep(1000);
                pr.write("100\n");
                pr.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
