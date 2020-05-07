package ring;

import java.net.Socket;

public class Utils {
    private static Frame token = new Frame();

    public static void generateToken(Socket managerSocket, Socket nextSocket){
        try {
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        token.isToken = true;
        // 消息通知放在此处，和生成Token绑定
        new Thread(new ProcessMessageSender(managerSocket, Message.TOKEN_SEND)).start();
        new Thread(new ProcessFrameSender(nextSocket, token)).start();
    }
}
