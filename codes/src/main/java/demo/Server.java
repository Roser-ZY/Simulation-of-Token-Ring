package demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket serverSocket;
    private static Socket socket;

    public static void main(String[] args) {
        try{
            System.out.println("Server启动");
            serverSocket = new ServerSocket(12222);
            while(true) {                                           // 服务器建立长连接
                socket = serverSocket.accept();
                new Thread(new ServerListen(socket)).start();
                new Thread(new ServerSent(socket)).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
