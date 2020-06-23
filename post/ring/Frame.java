package ring;

import java.io.Serializable;

public class Frame implements Serializable {
    public Boolean isToken;             // 判断是否为令牌
    public String sourceHost;           // 源主机号
    public String destinationHost;      // 目的主机号
    public String content;              // 数据内容，这里是Hello World
    public Boolean isReceived;          // 判断是否被接收
}
