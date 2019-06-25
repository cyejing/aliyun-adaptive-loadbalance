package com.aliware.tianchi;

import com.aliware.tianchi.stats.InvokerStats;
import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    @Override
    public void receiveServerMsg(String msg) {
        System.out.println("receive msg from server :" + msg);
        String[] split = msg.split(":");
        InvokerStats.getInstance().putBucket(split[0], Integer.valueOf(split[1]));
    }

}
