package demo;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.Scanner;

public class ClientSent implements Runnable {
    private Socket socket;

    ClientSent(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try{
            while(true){
                PrintWriter pr = new PrintWriter(socket.getOutputStream());
                Thread.sleep(2000);
                System.out.println("发送：Hello");
                pr.write("hello\n");
                //pr.write("\n");                     // 因为BufferReader必须遇到\n才能读取一行，如果没有\n，他就无法完成读取，即便已经flush也不行；也可以通过shutDownOutputStream来实现
                pr.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(socket != null) {
                    socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
