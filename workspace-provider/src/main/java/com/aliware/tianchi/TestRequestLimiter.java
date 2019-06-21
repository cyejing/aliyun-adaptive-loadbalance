package com.aliware.tianchi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.IOUtils;
import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.transport.RequestLimiter;

/**
 * @author daofeng.xjf
 *
 * 服务端限流 可选接口 在提交给后端线程池之前的扩展，可以用于服务端控制拒绝请求
 */
public class TestRequestLimiter implements RequestLimiter {

    private static final Logger log = LoggerFactory.getLogger(TestRequestLimiter.class);

    static {
        try {
           String json = loadResourceAsString("provider-conf.json");
            log.info(json);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String loadResourceAsString(String fileName) throws ClassNotFoundException {
        ClassLoader classLoader = getClassLoader();

        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load provider-conf.json,cause:" + e.getMessage(), e);
        }

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            try {
                return IOUtils.read(new InputStreamReader(url.openStream())).replace("\n", "").trim();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load provider-conf.json,cause:" + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Can not found provider-conf.json");
    }

    private static ClassLoader getClassLoader() throws ClassNotFoundException {
        return Class.forName("com.aliware.tianchi.policy.BaseConfig").getClassLoader();
    }

    /**
     * @param request 服务请求
     * @param activeCount 服务端对应线程池的活跃线程数
     * @return false 不提交给服务端业务线程池直接返回，客户端可以在 Filter 中捕获 RpcException true 不限流
     */
    @Override
    public boolean tryAcquire(Request request, int activeCount) {
//        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
//        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);
//        ThreadPoolExecutor executor = (ThreadPoolExecutor) executors.values().iterator().next();
//        int max = executor.getMaximumPoolSize();
//        if (activeCount+10 >= max) {
//            return false;
//        }
        return true;
    }

}
