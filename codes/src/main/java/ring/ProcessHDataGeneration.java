package ring;

import java.util.Date;
import java.util.Random;

public class ProcessHDataGeneration implements Runnable {
    private Random random = new Random((new Date()).getTime() % 62738);
    private int delay;
    private int hostIndex;
    private String destination;
    private static volatile Boolean status = true;

    public static void setStatus(){
        status = false;
    }

    public void run() {
        while(status) {
            hostIndex = 7;
            // 产生发送数据
            delay = 6000 + random.nextInt(15000);
            // 延时6~21s
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 防止将数据发送给自己
            while (hostIndex == 7) {
                // 随机选择一个目的主机
                hostIndex = random.nextInt(8);
            }
            destination = ProcessH.getHostIDs()[hostIndex];
            // TODO
            System.out.println(destination);
            // 将新的数据（这里用目的主机号代替）添加到队列中
            ProcessG.addDestination(destination);
            // 刷新状态位
            ProcessG.flushHasData();
        }
    }
}
