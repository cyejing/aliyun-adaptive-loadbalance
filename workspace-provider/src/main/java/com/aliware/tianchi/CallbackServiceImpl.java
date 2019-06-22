package com.aliware.tianchi;

import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author daofeng.xjf
 *
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 *
 */
public class CallbackServiceImpl implements CallbackService {

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);
        for (Entry<String, Object> entry : executors.entrySet()) {
            String port = entry.getKey();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) entry.getValue();
            int max = executor.getMaximumPoolSize();
            listener.receiveServerMsg(port + ":" + String.valueOf(max));
        }

    }
}
