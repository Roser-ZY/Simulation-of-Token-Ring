package demo;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
    private static Socket socket;
    private static Socket socket2Manager;
    private static Boolean connection_state = false;

    public static void main(String[] args) throws IOException  {
        System.out.println("Client启动");
        while(!connection_state) {
            System.out.println("重新连接。。");
            connect();
            try{
                Thread.sleep(3000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void connect(){
        try{
            socket = new Socket("localhost", 12222);        // 如果连接不上则会抛出异常，抛出异常时后面的代码不执行
            //socket2Manager = new Socket("localhost", 12223);
            new Thread(new ClientSent(socket)).start();
            new Thread(new ClientListen(socket)).start();
            //new Thread(new ClientSent(socket2Manager)).start();
            connection_state = true;
        }catch (IOException e){
            e.printStackTrace();
            connection_state = false;
        }
    }
}
