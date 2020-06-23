package ring;

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
