package demo;

import jdk.internal.util.xml.impl.Input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListen implements Runnable {
    private Socket socket;

    ClientListen(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try {
            InputStreamReader is = new InputStreamReader(socket.getInputStream(), "UTF-8");
            BufferedReader br = new BufferedReader(is);
            String info;
            while ((info = br.readLine()) != null) {
                System.out.println(info);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
