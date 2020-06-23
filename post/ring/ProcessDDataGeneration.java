package ring;

import java.util.Date;
import java.util.Random;

public class ProcessDDataGeneration implements Runnable {
    // TODO
    private Random random = new Random((new Date()).getTime() % 27362);
    private int delay;
    // TODO
    private int hostIndex;
    private String destination;
    private static volatile Boolean status = true;

    public static void setStatus(){
        status = false;
    }

    public void run() {
        while(status) {
            hostIndex = 3;
            // 产生发送数据
            delay = 6000 + random.nextInt(15000);
            // 延时6~21s
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 防止将数据发送给自己
            while (hostIndex == 3) {
                // 随机选择一个目的主机
                hostIndex = random.nextInt(8);
            }
            destination = ProcessD.getHostIDs()[hostIndex];
            // TODO
            System.out.println(destination);
            ProcessD.addDestination(destination);
            // 刷新状态位
            ProcessD.flushHasData();
        }
    }
}
