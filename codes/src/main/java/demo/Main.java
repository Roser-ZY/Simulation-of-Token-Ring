package demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static ServerSocket serverSocket;
    public static Socket socket;

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().exec("cmd /c start java demo.Server");
        Runtime.getRuntime().exec("cmd /c start java demo.Client");
        try{
            System.out.println("Main启动");
            serverSocket = new ServerSocket(12223);
            socket = serverSocket.accept();
            new Thread(new ServerListen(socket)).start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
