package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerListen implements Runnable {
    private Socket socket;

    ServerListen(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try {
            InputStreamReader is = new InputStreamReader(socket.getInputStream(), "UTF-8");
            BufferedReader br = new BufferedReader(is);
            String info;
//            while(true) {
//                System.out.println(br.readLine());
//            }
            System.out.println(br.readLine());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
