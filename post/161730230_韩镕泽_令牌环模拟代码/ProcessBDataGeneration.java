package ring;

import java.util.Date;
import java.util.Random;

public class ProcessBDataGeneration implements Runnable{
    private Random random = new Random((new Date()).getTime() % 47283);
    private int delay;
    private int hostIndex;
    private String destination;
    private static volatile Boolean status = true;

    public static void setStatus(){
        status = false;
    }

    public void run() {
        while(status) {
            hostIndex = 1;
            delay = 10000 + random.nextInt(15000);
            // 延时6~21s
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 防止将数据发送给自己
            while (hostIndex == 1) {
                // 随机选择一个目的主机
                hostIndex = random.nextInt(8);
            }
            destination = ProcessB.getHostIDs()[hostIndex];
            // TODO
            System.out.println(destination);
            ProcessB.addDestination(destination);
            // 刷新状态位
            ProcessB.flushHasData();
        }
    }
}
