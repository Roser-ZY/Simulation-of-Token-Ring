package ring;

public class GlobalStatus {
    private static volatile Boolean hasSetHostID = false;            // 判断是否已经设置主机号
    private static volatile Boolean hasStarted = false;              // 判断是否已经开始模拟

    // get
    public static Boolean getHasSetHostID(){
        return hasSetHostID;
    }
    public static Boolean getHasStarted(){
        return hasStarted;
    }

    // set
    public static void setHostID(){
        hasSetHostID = true;
    }
    public static void started(){
        hasStarted = true;
    }
    public static void reset(){
        hasSetHostID = false;
        hasStarted = false;
    }
}
