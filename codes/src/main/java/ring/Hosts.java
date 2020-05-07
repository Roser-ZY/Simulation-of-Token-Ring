package ring;

// 共享类，用于主机获取自己的ID以及目的主机的ID
public class Hosts {
    private static String[] hostIDs = {"1", "2", "3", "4", "5", "6", "7", "8"};

    public static String[] getHosts(){
        return hostIDs;
    }

    public static void setHosts(String[] HostIDs){
        hostIDs = new String[8];
        hostIDs = HostIDs;
    }
}
