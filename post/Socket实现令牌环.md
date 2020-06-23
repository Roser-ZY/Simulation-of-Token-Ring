# Socket实现令牌环

### Author：Roser Han

#### Location：Shandong Linyi

[TOC]

## 模拟要求

1. 一共8台主机，通过进程来模拟，主机号可以手动输入。
2. 产生一个令牌，一秒钟转移一次，即从一台主机到另一台主机需要一秒。
3. 每台主机每隔6~16秒（随机选择一个时间）产生一次数据，此数据的目的主机号也是随机从8台主机中选择，内容为`Hello World`。
4. 要截获令牌之后才能够发送数据，否则要等待令牌到来。
5. 数据从一台主机到另一台主机需要1.5秒，发送方向和令牌环传播方向一致。
6. 使用图形界面来设置主机号，同时运行过程也通过图形界面来显示。

## 令牌环

令牌环通常用于局域网，它的特点是使用一种**三字节**长度的遍历一个环形网络，这个帧就称为令牌。令牌环技术位于OSI模型的第二层——数据链路层。

通过令牌环能够让每一个主机都有公平的几乎发送数据，消除了基于竞争的访问方法，解决了冲突问题。

再令牌环局域网中的主机被组织为一个环形网络，每一个主机都只能有序地向下一个主机发送数据，这个数据发送收到令牌的控制。

### 工作过程

数据发送过程如下：

* 一个空的数据帧（令牌）在环中传递。
* 当一台主机需要发送数据时，它就会接收令牌，然后这台主机将自己的数据帧（从结构上看，可以看成是在令牌帧之后添加了数据部分）发送出去。
* 数据帧将会被下一台主机验证，如果下一台主机发现这个数据帧的目的地址是自己，则它将复制数据帧中的内容，并修改帧状态。
* 当这个数据帧返回发送主机时，发送主机会查看帧状态查看是否被正确接收，若数据被正确接收，则将移除数据帧中的数据。（没有正确接收的情况我还不太清楚）
* 这台主机发送新的令牌（和旧的令牌其实是一样的），然后继续在环中循环。

### 帧格式

![令牌环帧格式](E:\Catalog\VitalFiles\Study\Course\计算机网络\img\令牌环帧格式.png)

#### 令牌

令牌长度为3个字节，包括**开始定界符、访问控制字节和结束定界符**。

| Start Delimiter | Access Control | End Delimiter |
| --------------- | -------------- | ------------- |
| 8-bits          | 8-bits         | 8-bits        |

#### 中断帧

用于要发送数据的主机中断令牌传送。

| SD     | ED     |
| ------ | ------ |
| 8-bits | 8-bits |

#### 数据字段

数据帧携带高层协议的信息，而命令帧只有控制信息而没有上层协议的数据。

数据（控制）帧的大小，取决于信息域（即上层协议的数据）的大小。

| SD     | AC     | FC     | DA      | SA      | PDU from LLC(IEEE 802.2) | CRC     | ED      | FS     |
| ------ | ------ | ------ | ------- | ------- | ------------------------ | ------- | ------- | ------ |
| 8-bits | 8-bits | 8-bits | 48-bits | 48-bits | up to 4500x8 bits        | 32-bits | 8- bits | 8-bits |

#### 开始定界符/结尾定界符

定界符是一个标识数据帧开头和结尾的一个1字节长度的控制字符。具体的位就不看了，明白这是标识一个帧的首尾的就行。

#### 访问控制（需要）

这个字段包括以下这几个位：

| +    | Bits 0-2 | 3     | 4       | 5-7         |
| ---- | -------- | ----- | ------- | ----------- |
| 0    | Priority | Token | Monitor | Reservation |

前三位为优先级，Token就是标识是否为令牌帧，Monitor是活动监视器（Active Monitor）主机设置的，如果它看到了这个帧，就说明环中有这个帧，否则说明令牌帧丢失。最后三位为保留位。

Token = 0，令牌；Token = 1， 控制/信息帧。

防止无效数据帧在环路中无限循环，帧发出时，M=0，第一次经过监控站时，M=1，若之后再次经过监控站，则删除这个帧，并发出令牌。

#### 帧控制

帧控制字段长度为一个字节。

这个字段指明当前帧的内容是否包括数据或控制信息。

在控制帧中，这个字节指明指明控制信息的种类。

| +    | Bits 0-1   | Bits 2-7     |
| ---- | ---------- | ------------ |
| 0    | Frame type | Control Bits |

Frame type-**01**表明当前帧为LLC帧（IEEE802.2）并且忽略控制位；

**00**标识为MAC帧并且控制帧指明MAC控制帧的类型。

#### 目的地址（需要）

六字节的字段用于指明目的主机的物理地址。

#### 源地址（需要）

包含发送数据的主机的物理地址，这是一个六字节的字段，这个字段可以是发送站适配器为本地分配地址LAA（Local Assigned Address）或者为全局分配地址UAA（Universally Assigned Address）。

#### 数据（需要）

是一个可变长字段，最大长度为4500字节。

#### 帧检验序列

是一个四字节长度的字段，使用CRC方法进行检验。

#### 帧状态（需要）

一字节长度的字段，用于检验数据帧是否被识别并且被接收方复制。

| A     | C     | 0     | 0     | A     | C     | 0     | 0     |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| 1-bit | 1-bit | 1-bit | 1-bit | 1-bit | 1-bit | 1-bit | 1-bit |

A=1，被识别

C=1，被复制

### 活动和就绪监视器

每一个在令牌环网络中的主机要么是一个活动监视器主机（Active Monitor，AM），要么是一个就绪监视器主机（Standby Monitor，SM）。

同一时刻只能有一个活动监视器。这个活动监视器通过**选举或者监视器争用过程**来选出。

当以下事件发生时监视器征用过程将被执行：

* 检测到环上的信号丢失。
* 一个活动监视器主机没有被另一个主机检测到。
* 一个计时器即将超时，比如一个站在7秒内都没有看到令牌。

当以上任何情况发生时，一个主机就会发现当前网络中需要一个AM，它就会发送一个宣言令牌，宣布它想成为新的AM。如果这个令牌返回了发送者，那么它就会成为新的AM；如果其他主机同一时刻也想成为AM，那么这些主机中具有最大MAC地址的将会赢得选举过程，其他所有竞选主机成为SM。如果必要的话，所有的主机都应当有能力称为AM。

AM有几个超级管理员功能。

第一个功能是作为主时钟为环上的主机提供信号同步。

第二个功能是为环插入24-bit的延迟，确保环中总是有足够的缓存保证令牌进行循环。

第三个功能是确保当没有帧传送时只有一个令牌在循环，并且检测环是否被打破。

最后一个功能是能够从环上移除令牌。

### 令牌插入程序

这一部分了解一下就好，我们这里不模拟这部分。

令牌环的主机必须经过5阶段的环插入过程才能加入到令牌环网络。如果任何一个阶段失败，令牌环的主机就不能加入到环网络并且令牌环驱动将会报告一个错误。

* 阶段0——叶检查

  一台主机先进行叶媒体检查，一个主机被封装在MSAU中并且能够发送2000个测试帧到它的发送对，然后再循环回到接受对，主机以此确保它可以无差错地收到这些帧。

* 阶段1——物理插入

  一台主机发送一个5V的信号到MSAU来开启继电器。

* 阶段2——地址确认

  在令牌环帧的目的地址字段中，工作站使用自己的MAC地址传输MAC帧。当这个帧返回时，如果其AR和FC位都为0（说明当前环中没有与之使用相同MAC地址的主机），该站必须参与周期性轮询过程。这时主机识别它们自己在网络中作为MAC管理功能的一部分。

* 阶段3——加入环轮询

  一个主机获取它活动上游邻居的地址，并将它自己的地址告诉最近的下游邻居，从而创建了环形地图。主机在收到AR和FC位都被设为0的AMP或SMP帧之前都会等待。当收到这种帧之后，如果资源充足，主机就会将AR和FC设置为1，并对SMP帧进行排队以进行传输。如果在18秒内都没有接收到这样一个帧，这个主机就会报告一个打开错误并从环中退出。如果这个站成功加参与环轮询，它就进入最后一个阶段，初始化请求。

* 阶段4——初始化请求

  最后一个站点向参数服务器发送一个特殊的请求来获取配置信息，这个帧被发送到一个特殊的功能地址，通常是一个令牌环桥，它可能包含定时器和这个新的主机需要知道的环号信息。

### 优先级（待定）

截获令牌时，优先级大于等于令牌内PPP的站点才能截获。

如果一台主机接收帧时发现目的地址不是自己，而且自己又有要发送的数据，则可以进行预约，将本站的优先级写入该帧的预约位（三位，一共7个优先级）。升高优先级的站点发送完数据后，要将令牌的优先等级降低为原来的优先级。

| Priority bits | Traffic type                                        |
| ------------- | --------------------------------------------------- |
| 000           | Normal data traffic                                 |
| 001           | Not used                                            |
| 010           | Not used                                            |
| 011           | Not used                                            |
| 100           | Normal data traffic（forwarded from other devices） |
| 101           | Data sent with time sensitivity requirements        |
| 110           | Data with real time sensitivity                     |
| 111           | Station management                                  |

## 线程与进程

你可能会想，我模拟令牌环为什么还要和线程和进程扯上关系？

你也可能不这么想。

总而言之，在开始之前，我们一定要明确线程与进程的关系，虽然我本人也还在学习，有一些奇奇怪怪的问题也都没有得到结论。

什么是进程，进程是一个动态的概念。一个程序可以打开多次，每一次都会产生一个进程，这些进程之间是并发进行的。何为并发？简单理解就是，一会执行进程A，一会执行进程B，一会又去执行进程A，一会去执行进程C。注意，并发不等于并行，二者有本质差别。

什么是线程呢？线程是后来引入的概念。线程作为任务分派的基本单位，进程为资源分配的基本单位。比如，我创建了一个进程，这个进程需要被分配各种资源，内存，CPU等等，那么在这个进程中，如果我想接收一个来自客户的消息，他说想要发送一个文件给我，我就必须停下手头的事去等待接收文件，但是往往服务器的某个进程是不能够因为一个客户请求而停止其他工作的，这时候，线程就发挥作用了。

一个进程中至少有一个线程（通常就是主线程，Java里就是main函数），当有多个线程的时候，这些线程之间就是并发的，它们可以共享进程的资源，同时也可以有自己的小资源，线程之间可以通过一些共享变量（没有那么高大上）相互交流。

这样，我们前面的问题就解决了。当服务器进程收到来自客户的消息，要接受文件时，它会创建一个线程去等待这个文件，同时，其他线程仍在有条不紊地进行，等我接受完这个文件之后，这个接收线程随之关闭。

看到这里，对于这个项目应该开朗了一些了。

（再强调一下上面可能有些地方说的没那么严格准确，但是为了讲明白，讲的通俗了些）

## Socket基础

我们的令牌环的实现不需要什么高深的Socket技术，只需要掌握两点：Socket客户端和Socket服务器。

在开始之前，我们首先要知道两台主机之间是如何通信的。抛开计算机网络的多个模型，在我们的这个实现中，只考虑TCP/IP协议，当然，如果你不了解也没关系，我们的连接过程是面向TCP的，知道这个就可以了。

首先，计算机要通过一定的协议规则来进行通信，在这里基本思路就是，我们的计算机的某个应用要通过某个端口向外发送数据，而接收消息的计算机从相同的端口接收数据，我们怎么知道传递给哪个主机以及接收消息的主机怎么直到谁传给了呢？没错，我们还需要知道两台主机的IP地址。

我们说过，这里我们只用到TCP连接，即三次握手和四次握手的机制，这里不深入讨论。

知道这么个基本过程，我们就可以开始Socket编程了。

首先，Socket编程需要一方为服务器，一方为客户端，这里并不是说另一台主机一定是一个服务器，只不过他们的通信很像客户端-服务器通信。

数据有时怎么发送出去的呢？通过输入输出流。

客户端Socket创建输出流，将输出的数据写入，然后再发送给服务器Socket，服务器Socket创建输入流，接收来自客户端Socket的数据，然后它也可以创建一个输出流，将回复消息写入，发送给客户端Socket，再然后，客户端Socket创建一个输入流，接收来自服务器Socket的反馈。

这样，一次通信就完成了。至于进程之间的通信，可以学习一下操作系统。

明白了这些，我们的这次模拟实验就足够了。

当然，可以举个例子，这个例子是来自于网络的。

首先创建Client。

```java
package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        while(true) {
            try {
                // 创建客户端Socket，输入服务器的IP或者域名（这里是本地主机，所以localhost就可以了）和端口号
                Socket socket = new Socket("localhost", 8888);
                // 创建一个输出流
                PrintWriter os = new PrintWriter(socket.getOutputStream());
                // 写入内容：你好
                System.out.println("内容：你好");
                // os的写入方法有很多，可以学习《Java TCP/IP Socket编程》，也可以自行百度，这里写入字符串使用write就好
                os.write("你好");
                // 将缓冲区的内容送出
                os.flush();
                // 客户端Socket关闭输出
                socket.shutdownOutput();

                // 创建输入流
                InputStreamReader is = new InputStreamReader(socket.getInputStream());
                // 创建一个缓冲区，用于读，这样方便一些，直接通过is来读也未尝不可
                BufferedReader br = new BufferedReader(is);
                String info = null;
                while ((info = br.readLine()) != null) {
                    System.out.println("回应:" + info);
                }
                // 关闭流和缓冲区
                os.close();
                is.close();
                br.close();
                // 关闭Socket
                socket.close();
            }catch (ConnectException ce){ // 这个是防止你先运行了Client，后运行Server，如果这样就会报错，但是现在catch这个异常，就可以忽视这个错误
                continue;
            }
        }
    }
}

```

然后创建Server。

```java
package demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String args[]) throws IOException {
        while(true) {
            int port = 8888;
            String info = null;
            // 这里监听端口8888，如果Client发送数据，会发送到8888端口
            ServerSocket serverSocket = new ServerSocket(port);
            // 这里创建一个客户端Socket，调用ServerSocket.accept()返回向这个端口发送数据的客户端Socket
            Socket socket = serverSocket.accept();
            // 之后的内容和Cliend一样了，我就不多写了
            InputStreamReader is = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(is);
            while ((info = br.readLine()) != null) {
                System.out.println("内容:" + info);
            }
            socket.shutdownInput();
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            os.write("Received");
            os.flush();
            socket.shutdownOutput();
            is.close();
            os.close();
            br.close();
            socket.close();
            serverSocket.close();
        }
    }

}
```

我们在IDEA中运行这两个程序，会产生两个进程（实际上应用层通信就是两个进程之间的通信）。

运行结果：

![Client运行结果](E:\Catalog\VitalFiles\Study\Course\计算机网络\img\Client运行结果.png)

![Server运行结果](E:\Catalog\VitalFiles\Study\Course\计算机网络\img\Server运行结果.png)

只有这些肯定还不够，我们还需要服务器保持长连接，使用线程监听，这样我们才能够建立起一个能够同时发送和接收数据的主机。

### 建立长连接

在上面我们不断循环创建连接，关闭连接，如果不这样我们就只能发送一条消息，但是这样又会造成多次创建的资源消耗。于是，可以使用长连接来保持通信。

其实长连接就是个叫法，简单来说就是建立一次连接，写一个死循环（有些时候是**活**循环）保持连接。但是要注意，不能关闭输入输出流，因为如果关闭，Socket会以为你已经关闭了Socket然后断开连接（细节还不太明白，但是我试验过，关掉之后就会抛出异常）。

### 进程间的通信

在前面的例子中看得很清楚，进程之间通过I/OStream进行交流但是不同的Stream能够传输的数据不同，最强大的应该是`DataInputStream/DataOutputStream`，当然我并非只用了这一种。

注意我们写入输出流之后一定要`flush`，这个方法就是刷新缓冲区，只有刷新之后接收方才能够接收到。

如果只是读写字符串，还要特殊一些，要添加`\n`换行符在接收端才能够通过`readLine`接收到，否则就会一直等待（原理我就不说啦）。

#### DataInputStream/DataOutputStream

这种方式我在主进程与主机进程交流的时候使用，主要是传输枚举类型的消息，细节之后再说，它可以写入基本类型以及字节流（这个比较复杂，这里不多说了，没有用到），这里我只使用了`writeInt`和`readInt`，因为枚举类型本质上就是整型。

#### ObjectInputStream/ObjectOutputStream

很显然，这种方式可以用来读写对象，确实，这个项目中我就是用这种方式来进行帧的传递的。主要使用了`writeObject`和`readObject`。

本来以为用的很多，其实就用了这俩。hah这俩就够用啦。

## Socket模拟令牌环运行

### 基本思路

我用的语言是Java，当然也可以用其他语言，思想是一样的，工具完全是建立在思想之上的，不要纠结于工具。

首先，我们要输入8个主机号，然后点击开始运行，开始运行之后就生成一个环型网络，然后开始令牌环模拟。

运用前面的例子，让一台主机既作为客户端Socket（后一台主机的客户端），也作为服务器Socket（前一台主机的服务器）。

为了简单，我们按照前面给出的令牌帧和数据帧，去除掉一些字段，只使用我们需要的字段，这个后面再详细说一说。

算法其实没啥，主要内容就是令牌环的传递、产生和截获，数据的发送、接收和复制。

注意，我们需要一个主进程来作为展示，用它创建其他几个进程，其他几个进程还需要将自己的反馈信息发送给这个主进程。

这里就又来了个问题。我们需要创建进程，那么就需要调用其他几个进程的main函数。

通过`Runtime`这个类来打开其他进程。

```java
Runtime.getRuntime().exec("command");
```

这个类用了单例模式，所以就没再创建变量，直接获取实例调用方法就好了，后面得command有很多种形式，后面会说这个项目中是怎么打开的，用了什么命令。

### 数据结构

好了，正式来看一下数据结构。

这个类图是我写完才画的，可能有些地方并没有那么合理，也没那么美观，将就着看。

#### 消息

```java
public enum Message {
    // 主进程->主机
    SET_ID,                         // 设置HostID
    CHOSEN_AM,                      // 选择AM，生成令牌以及删除令牌
    START_RING,                     // 开始运行
    RESET_RING,                     // 重置

    // 主机->主进程
    TOKEN_ARRIVED,                  // 令牌到达此处
    TOKEN_RECEIVED,                 // 截获令牌
    TOKEN_SEND,                     // 令牌生成并发送
    DATA_SEND,                      // 数据产生并发送
    DATA_ARRIVED,                   // 数据转发
    DATA_RECEIVED,                  // 数据接收
    DATA_ARRIVED_SOURCE_N,          // 数据回到源主机，但数据未被接收
    DATA_ARRIVED_SOURCE_Y,          // 数据回到源主机，数据被接受
    RESET_COMPLETE,                 // 完成重置
}
```

#### 主机进程

```java
public class ProcessA {
    private static String hostID;                                               // 主机号
    private static String[] hostIDs = new String[8];                            // 所有主机的主机号
    private static Boolean ifReset = false;                                     // 判断环是否处于重置状态，平时为false，当得到reset消息时变为true，但是只有
    private static Boolean hasData = false;                                     // 判断是否有准备发送的数据，如果有监听线程就要停止转发令牌，并告知进程收到令牌
    private static Boolean isAM = false;                                        // 是否为活动监视站
//    private static int frameTimes;                                            // 帧经过次数，令牌或者数据，用于监测站
    private static Queue<String> destinationID = new LinkedList<String>();           // 存放目的主机号
    private static Frame receiveData = null;                                    // 接受的数据内容
    private static ServerSocket serverSocket;                                   // 作为前一台主机的Server
    private static Socket clientForProcessSocket;                               // 作为客户Socket向之后的主机发送数据
    private static Socket clientForManagerSocket;                               // 作为客户Socket向Manager发送数据
    private static Socket preSocket;                                            // 前一台主机的Socket
    private static Boolean clientForProcessStatus = false;                      // 判断主机是否连接上后一台主机
    private static Boolean clientForManagerStatus = false;                      // 判断主机是否连接上后一台主机
    
    // get
    public static String getHostID();
    // 获取所有的主机ID，用于生成数据时使用
    public static String[] getHostIDs();
    public static Boolean getIfReset();
    // 判断是否有数据要发送
    public static Boolean ifHasData();
    // 判断当前主机是否为AM
    public static Boolean ifIsAM();
    // 获取当前数据的目的主机号，用于Sender
    public static String getDestination();
    
     // set
    public static void setHostID(String HostID);
    public static void setHostIDs(String[] HostIDs);
    // 重置ifReset状态位
    public static void resetIfReset();
    // 拷贝当前的数据帧到主机中
    public static void copyFrame(Frame frame);
    // 刷新此状态位，如果待发送信息的队列中仍有数据则为置状态位为true，否则为false
    public static void flushHasData();
    // 添加目的主机号到队列中（代替整个数据，只保存主机号）
    public static void addDestination(String destination);
    public static void setIsAM();
    public static void reset();
    
    // 与下一台主机连接
    public static void connectNextProcess(int port);
    
    public static void main(String[] args);
}
```

##### 主机数据生成

```java
public class ProcessADataGeneration implements Runnable{
    private Random random = new Random((new Date()).getTime() % 23873);               // 随机种子
    private int delay;                                                                      // 过多久产生一次数据
    private int hostIndex;                                                                  // 当前主机的主机号在数组中的索引，用于判断目的主机是否为源主机，如果是则重置
    private String destination;                                                             // 目的主机号，随机选择
    private static volatile Boolean status = true;                                          // 通过状态位来判断是否结束线程

    public static void setStatus();
}
```

##### 主机数据监听

````java
public class ProcessADataListener implements Runnable {
    private Socket socket;              // 被监听的主机的客户端Socket（即前一台主机的Socket）
    private Socket nextSocket;          // 下一台主机的服务器socket
    private Socket managerSocket;       // 主进程，用于发送信息给主进程
    private Frame frame;                // 收到的数据帧
    // 模拟未接收的情况
    private Random random = new Random(((new Date()).getTime() % 2846));

    ProcessADataListener(Socket socket, Socket nextSocket, Socket managerSocket);
}
````

##### 主机数据发送

````java
public class ProcessADataSender implements Runnable {
    private Socket nextSocket;                                  // 下一台主机的Socket
    private Socket managerSocket;                               // 主进程Socket
    private String destination;                                 // 目的主机
    private Frame frame = new Frame();                          // 数据帧

    ProcessADataSender(Socket nextSocket, Socket managerSocket);
}
````

##### 主机消息监听

```java
public class ProcessAMessageListener implements Runnable {
    private String[] hostIDs = new String[8];                   // 用于读取所有的主机号
    private Socket managerSocket;                               // 监听目标的Socket，这里是主进程的Socket
    private Socket nextSocket;                          // 下一台主机的Socket
    private Message MESSAGE = null;                     // 接收到的消息，消息每接收一次，就置为null，表示无消息

    ProcessAMessageListener(Socket managerSocket, Socket nextSocket);
}
```

#### 主进程

```java
public class RingManager {
    private static int chosedHost;                                              // 从0~8，然后通过switch选择发送给谁
    private static String[] hostIDs = {"1", "2", "3", "4", "5", "6", "7", "8"}; // 获取输入的主机号
    private static ServerSocket[] serverSockets = new ServerSocket[8];          // 8个服务器Socket，监听8台主机的消息
    private static Socket[] socket = new Socket[8];                             // 8个服务器Socket获取到的主机Socket

    // UI
    private static JFrame jf;                                                   // JFrame
    // 布局
    private static Box mainLayout;                                              // 主布局，左右两部分
    private static Box leftLayout;                                              // 左布局，包括主机号输入，状态展示以及状态信息
    private static Box rightLayout;                                             // 右布局，包含三个按钮
    private static Box hostIDInputLayout;
    private static Box showLayout;
    private static Box statusLayout;

    // 主机号
    // 标签
    private static JLabel[] hostIDLabel = new JLabel[8];
    // 输入框
    private static JTextField[] hostIDInput = new JTextField[8];
    // 输入Panel
    private static JPanel[] hostIDPanel = new JPanel[8];

    // 展示区域
    private static JLabel statusPicture;

    // 状态报告区域
    private static JTextArea statusReport;
    private static JScrollPane statusJsp;

    // 按钮
    private static JButton setHostIDButton;
    private static JButton startButton;
    private static JButton resetButton;
    // 按钮panel
    private static JPanel setHostIDPanel;
    private static JPanel startPanel;
    private static JPanel resetPanel;
    
    // get
    public static String[] getHostIDs();		// 获取主机号
    
    // set
    public static void setSetHostIDButton();	// 在MessageListener中设置主进程中的set Host ID Button为可用状态
    public static void setStatus(String status);// 在状态栏显示状态信息
    public static void setStatusPicture(String name);	// 设置显示部分的图片
    // 创建用户界面
    public static void createUI();
    
    // 主进程
    public static void main(String[] args);
}
```

##### 主进程的消息监听

```java
// 创建消息监听线程
public class ManagerListener implements Runnable {
    private int listenHost;                         // 标识监听的主机
    // 存放显示data传送的图片名
    private String[] pictureNames_D = {"data-I.png", "data-II.png", "data-III.png", "data-IV.png", "data-V.png", "data-VI.png", "data-VII.png", "data-VIII.png"};
    // 存放显示token传送的图片名
    private String[] pictureNames_T = {"token-I.png", "token-II.png", "token-III.png", "token-IV.png", "token-V.png", "token-VI.png", "token-VII.png", "token-VIII.png"};
    // 存放数据被正确接收的图片名
    private String[] pictureNames_RY = {"data-Y-I.png", "data-Y-II.png", "data-Y-III.png", "data-Y-IV.png", "data-Y-V.png", "data-Y-VI.png", "data-Y-VII.png", "data-Y-VIII.png"};
    // 存放数据未被正确接收的图片名
    private String[] pictureNames_RN = {"data-N-I.png", "data-N-II.png", "data-N-III.png", "data-N-IV.png", "data-N-V.png", "data-N-VI.png", "data-N-VII.png", "data-N-VIII.png"};
    private Socket socket;                          // 监听主机的Socket
    private Message MESSAGE;                        // 监听收到的消息
    
    // 构造函数
    ManagerListener(Socket socket, int listenHost);
}
```

##### 主进程的消息发送

```java
public class ManagerSender implements Runnable {
    private Socket socket;
    private Message MESSAGE;

    ManagerSender(Socket socket, Message MESSAGE);
}
```

##### 数据接收

```java
// 主要是将数据内容输出
public class ProcessDataReceiver implements Runnable {
    private Frame frame;

    ProcessDataReceiver(Frame frame);
}
```

##### 帧转发

```java
// 通过线程将帧转发
public class ProcessFrameSender implements Runnable {
    private Socket socket;
    private Frame frame;

    ProcessFrameSender(Socket socket, Frame frame);
}
```

#### 帧

```java
public class Frame implements Serializable {
    public Boolean isToken;             // 判断是否为令牌
    public String sourceHost;           // 源主机号
    public String destinationHost;      // 目的主机号
    public String content;              // 数据内容，这里是Hello World
    public Boolean isReceived;          // 判断是否被接收
}
```

#### 全局状态

````java
// 用于控制主机发来的消息对于整个系统的影响
public class GlobalStatus {
    private static volatile Boolean hasSetHostID = false;            // 判断是否已经设置主机号
    private static volatile Boolean hasStarted = false;              // 判断是否已经开始模拟

    // get
    public static Boolean getHasSetHostID();
    public static Boolean getHasStarted();
    
    // set
    public static void setHostID();
    public static void started();
    public static void reset();
}
````

##### 令牌生成工具

```java
// 因为直接总是要生成令牌导致代码重复，因此单独摘出来
public class Utils {
    private static Frame token = new Frame();

    public static void generateToken(Socket managerSocket, Socket nextSocket);
}
```

#### 类图

![类图](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\类图.jpg)

### 主进程

主进程负责图形界面，并统筹所有主机的发送过程，比如现在主机A正在发送令牌环给B，就要从图形界面中展示出来，通过建立一个状态栏，显示状态，另外主进程还需要负责将我们输入的主机号发送给各个主机，然后各个主机初始化自己的主机号作为标识。

主进程作为一个总服务器，需要监听多个端口。

主进程中要设置监听线程，监听线程要不断地循环监听来自各个主机的消息，通过成员变量与主线程进行交流。

而发送线程不需要循环，只需要在有需要的时候发送一次即可。

#### 选择活动监视站

前面我们说，一个令牌环中必须要有一个活动监视站，用于监视是否有无效数据在传送，并且监视令牌环是否还存在。

按道理活动监视站是要竞选的，我们为了方便直接随机指定一个，因为竞选的过程比较复杂，我也不太了解具体的竞选流程。

活动监视站要能够做到生成令牌和删除令牌。

（当然还可以监测是否有无效数据帧，是否有令牌环等）。

注意：**我们这里的活动监视站，是选中的第一个发送令牌的主机**。

#### 流程图

##### 主机进程

![主机进程流程图](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\主机进程流程图.jpg)

##### 数据生成

![数据生成流程图](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\数据生成流程图.jpg)

##### 数据监听

![数据监听流程图](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\数据监听流程图.jpg)

### 主机进程

主机进程需要完成的工作主要是，从主进程获取主机号并更新，如果自己有数据要发送，需要将这个消息发送给主进程，如果令牌环循环到此处或者数据到此处需要报告，如果自己获取到了数据也要报告，总而言之，就是各种情况都要向主进程报告。

另外主机进程还要自己生成数据，在随机的几秒内产生一个数据，然后等待，等自己接手令牌后发送，另外这个数据的目的主机需要随机生成，我们将所有的主机号在每个主机进程中都存放一次，方便生成帧中的目的主机号。

主机中也要设置监听线程和发送线程。

同样，监听线程要不断监听，有两个监听线程，一个负责监听主进程，另一个负责监听此主机的前一个主机进程。

因此，我们的主机需要四种线程：两个负责与主进程的消息交流，两个负责数据的接收和发送。

负责数据接收的线程在没有数据要发送的时候直接转发，不再回到主机进程，当有数据要发送时则改变某些状态位，不再转发，发送过程由主机进程操作。

当接收数据时，线程调用`copy`函数将数据帧复制到主机进程中的帧，模拟复制过程。

#### 数据产生与发送

数据由主机自己产生，当有数据时也可以继续产生数据，要设置一个**数据队列（仅存放目的主机号）**，新产生的数据要等待前面的数据发送完成。

数据发送前要检查是否有令牌，接收到令牌的时候检查自己的消息队列是否为空，如果不为空说明有数据要发送，此时将队列中第一个数据（实际上是一个主机号）取出，封装到数据帧中，然后发送给下一台主机。

发送完成后，要发送一个消息给主进程，告知我此时发送了一个数据。

之后每一台主机接收到数据帧，都要报告，直到某一台主机将数据复制到自己的缓冲区之后，剩下的主机报告*已被接受*。

当数据帧回到源主机时，检查是否被接受，并报告情况，然后发送令牌。

当接收到令牌时，也要向主进程发送消息，报告接收到令牌，有/无数据发送。

**我们将数据的各个字段封装在对象中**。

**此时Socket发送的不再是字符串而是对象了，可以使用json来操作，但是这个还要学习成本，所以我们这里实现对象Serializable，用在Frame上，然后就可以通过ObjectInputStream和ObjutOutputStream来读取和输出了**。

#### 数据接收

数据接收时，按道理是要将数据复制到自己的缓冲区的，当然我们这里没办法实现两个过程：即查看和复制。我们这里全部都是读取出来，然后转发出去，接收时将数据复制到自己的接收数据中，然后发送数据，此时发送数据要判断其是否为令牌，如果不是令牌，继续判断其源主机是否为自己，如果不是则直接转发。如果是令牌，则将自己的数据发送出去。

这里的接收数据非常重要，我们需要通过监听线程将监听到的数据保存到接收数据中，然后进行一系列判断之后新建发送线程。

#### 活动监视站

活动监视站的工作上面已经提到了，具体说一下实现流程。

主进程控制的开始模拟和重置模拟，需要与AM交流，这时候直接向以选为AM的主机通信即可。

而对于检测无效帧和令牌的操作，则需要

##### 生成令牌

生成令牌其实就是发送一个令牌帧，很容易。

##### 删除令牌

在图形界面中有一个删除令牌的按钮，点击后令牌到达活动监视站时由活动监视站删除（删除后要告知主进程，将活动监视站的主机号设为-1），此时系统中没有令牌，也没有了活动监视站。

之后必须重新点击开始运行，此时会新选择一个活动监视站。

#### 流程图

![主进程流程图](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\主进程流程图.jpg)

## 概览

![消息交互](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\消息交互.jpg)

## TODO

还有一些非必要的功能没有实现：

1. 发出reset消息后，调整Token优先级，只让AM能够截获并删除。
2. 添加预约功能，某台主机有数据要发送但是收到的为令牌帧时，向其中写入预约优先级，然后下一次令牌生成时将优先级写入，同时此主机的优先级应该设置为同等优先级，这样这台主机下次能够更快收到Token。还要注意写入一次优先级之后就不要再写入了，会造成冲突。

## Q&A

#### Q：maven项目中，在类目录下使用命令`java ***`报错：找不到或无法加载主类

A：在IDEA的maven项目中，`.java`文件在IDEA中运行之后会自动将生成的`.class`放到`target\classes`中，当我们在`.java`文件目录下使用`java ***`会出错，应该到`target\classes`下运行。

另外还要注意一点，如果放入了包中，我们需要这样运行：`java packagename.***`，当前的目录应该位于包的上一个目录才可以。

这个问题会导致什么呢？

我们需要使用`Runtime`实例的`exec()`，在这里面我们需要用命令来运行进程，而我们要执行的类在`target\class`中，所以就需要到这个文件夹去运行。

简单来说，我们要通过程序加载某些文件或程序时，**要看编译后的路径**，而不是写代码的时候看到的路径。

#### Q：在IDEA中使用`Runtime.getRuntime().exec(cmd)`发现没有反应。

A：我们要创建多个进程，我们就一定要用到Runtime或者ProcessBuilder，但是我们使用的这个代码：

```java
Runtime.getRuntime().exec("java ***");
```

他的运行是不会出现任何反应的，而且运行这句话的进程在运行完这条语句之后如果没有其他语句，就会退出，但是如果打开的这个进程没有运行完，他就会一直在后台运行，为了能够看到这些进程，我们将命令改为：

```java
Runtime.getRuntime().exec("cmd /k start java ***");
```

这条语句的效果和上面的一样，但是他会打开cmd，如果我们的进程是一直运行的，他就会始终存在，如果出错的话，会一下子闪出错误信息然后退出（实际上快得看不清）。

所以运行这个程序的时候如果发现程序一闪而过，最好是检查一下Client和Server之间的关系是否正确，代码逻辑是否正确，往往出错就会直接退出了。

#### Q：Socket报告：java.net.SocketException:socket closed

A：这个问题是因为一个Socket如果输入输出流关闭就会自动关闭，因此关闭输入输出应该在最后，不要频繁关闭。

#### Q：Socket报告：java.net.SocketException:Connection reset

A：两种情况，（答案来自网络）第一个就是如果一端的Socket被关闭（或主动关闭或者因为异常退出而引起的关闭），另一端仍发送数据，发送的第一个数据包引发该异常(Connect reset by peer)。另一个是一端退出，但退出时并未关闭该连接，另一端如果在从连接中读数据则抛出该异常（Connection reset）。简单的说就是在连接断开后的读和写操作引起的。 

#### Q：我在主进程中更改了全局变量，而主机进程中检测不到。

A：这个问题很傻，这是两个进程，在这两个进程里全局变量的状态是不同的，除非将全局变量的值也传过去。

####  Q：直接发送一连串消息（枚举类型）给主进程时报错，`java.lang.ArrayIndexOutOfBoundsException`。

![枚举类型消息发送出错](E:\Catalog\VitalFiles\Study\Course\计算机网络\课程设计\img\枚举类型消息发送出错.png)

A：问题就在于，这里使用了线程，线程创建后是并发执行的，它们操作的还是同一个缓冲区，可能第一个线程在写入之后，还没有`flush`，第二个线程紧接着写进去，导致缓冲区的数据变得混乱不可分，这时候`flush`之后就会导致发送的数据出错（未知的错误，比如发送了错误的消息，或者数组越界，因为这里使用的枚举类型发送数据，从int转换为枚举使用的方法可能会造成数组越界）。

可以延时几秒再发送下一个，这样能够解决这个问题。

#### Q：在DataListener线程中改变了ProcessA中的某个状态变量，在DataSender中监测不到。

A：暂时还没找到原因。

#### Q：两个字符串使用`==`进行比较出错。

A：字符串内容的比较需要`equals`函数。

使用`==`是两个字符串的**引用**进行比较，判断是否指向同一个对象。

注意，如果两个字符串都是null，那么它们的`==`结果为`true`。

Java中使用的是文字池，有兴趣的可以自行学习一下。

#### Q：创建进程后没反应。

A：这个问题经常忘记，创建完进程还要`start()`才能够运行啊！

#### Q：通过`.class.getClassLoader.getResource().getPath()`无法正确得到路径，因为路径包含中文。

A：这种情况下得到的路径是有乱码的，它会将中文通过转义字符表示出来，这不是我们想要的。

要么将路径中的中文改成英文，或者使用`.class.getClassLoader.getResource().toURL().getPath()`，这种方式需要处理一个异常。
