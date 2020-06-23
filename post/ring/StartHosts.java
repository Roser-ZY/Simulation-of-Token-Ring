package ring;

public class StartHosts implements Runnable {
    public void run() {
        try{
            // 延时打开进程
            //Thread.sleep(5000);
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessA");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessB");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessC");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessD");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessE");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessF");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessG");
            Runtime.getRuntime().exec("cmd /k start java ring.ProcessH");

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
