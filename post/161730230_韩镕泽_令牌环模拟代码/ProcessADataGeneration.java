package ring;

import java.util.Date;
import java.util.Random;

public class ProcessADataGeneration implements Runnable{
    private Random random = new Random((new Date()).getTime() % 23873);               // 随机种子
    private int delay;                                                                      // 过多久产生一次数据
    private int hostIndex;                                                                  // 当前主机的主机号在数组中的索引，用于判断目的主机是否为源主机，如果是则重置
    private String destination;                                                             // 目的主机号，随机选择
    private static volatile Boolean status = true;                                          // 通过状态位来判断是否结束线程

    public static void setStatus(){
        status = false;
    }

    public void run() {
        while(status) {
            // 要重置，否则下面的while只执行一次，做不到随机选择主机的目的
            hostIndex = 0;
            // 产生发送数据
            delay = 6000 + random.nextInt(15000);
            // 延时6~21s
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 防止将数据发送给自己
            while (hostIndex == 0) {
                // 随机选择一个目的主机
                hostIndex = random.nextInt(8);
            }
            destination = ProcessA.getHostIDs()[hostIndex];
            // TODO
            System.out.println(destination);
            // 将新的数据（这里用目的主机号代替）添加到队列中
            ProcessA.addDestination(destination);
            // 刷新状态位
            ProcessA.flushHasData();
        }
    }
}
