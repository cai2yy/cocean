package app.armot.core;

import annotations.Inject;
import annotations.Named;
import annotations.Singleton;
import app.armot.api.bean.Component;
import app.armot.api.bean.Device;
import app.armot.api.service.ComponentService;
import app.armot.utils.Configuration;
import core.Injector;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Cai2yy
 * @date 2020/2/21 19:34
 */

@Singleton
public class ArmOT {

    @Inject
    EventBus eventBus;

    public Map<Integer, Device> getDevices() {
        return devices;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public Map<Integer, String> getComponentNameDict() {
        return componentNameDict;
    }

    @Inject
    @Named("ComponentService")
    ComponentService componentService;

    private final int workerThreads = Configuration.workerThreads;
    EventExecutorGroup executors;

    private final static Logger LOG = LoggerFactory.getLogger(ArmOT.class);

    Map<Integer, Device> devices = new ConcurrentHashMap<>();
    Map<String, Component> components = new ConcurrentHashMap<>();
    Map<Integer, String> componentNameDict = new ConcurrentHashMap<>();

    public ArmOT() {
        // 初始化总线程池
        var threadFactory = new ThreadFactory() {
            AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ArmOT-" + seq.getAndIncrement());
                return t;
            }
        };
        //todo 采用netty自带的NioEventLoop线程池
        executors = new NioEventLoopGroup(workerThreads, threadFactory);
        LOG.info("ArmOT线程池成功创建: 线程池类型: {}, 线程数: {}", executors.getClass().getSimpleName(), workerThreads);

        LOG.info("ArmOT成功创建");
    }

    public ArmOT asyncInit() {
        asyncInitComponent();
        asyncUpdateDevices();
        return this;
    }

    public void asyncInitComponent() {
        LOG.info("开始扫描插件");
        Future<Integer> initComponentFuture = executors.submit(() -> componentService.init());
        initComponentFuture.addListener((FutureListener<Integer>) integerFuture -> {
            var count = integerFuture.getNow();
            LOG.info("---------- 扫描完毕,共有插件{}个 ----------", count);
            var var1 = 1;
            for (String name : components.keySet()) {
                LOG.info("插件{}[{}]: {} ", var1++, name, components.get(name));
            }
        });
        //todo 写入yml配置文件
    }

    public void asyncUpdateDevices() {

    }

}
