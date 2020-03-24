package app.armot.core;

import annotations.Inject;
import annotations.Singleton;
import core.Injector;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cai2yy
 * @date 2020/2/21 22:03
 */

@Singleton
public class EventBus {
    // Event: receiveMqttMsg, Args: MqttMessage message
    @Inject
    ArmOT armOT;

    Map<String, List<Listener>> eventRegistry;

    EventExecutorGroup executors;

    private final static Logger LOG = LoggerFactory.getLogger(EventBus.class);

    public EventBus() {
        this.eventRegistry = new ConcurrentHashMap<>();
        LOG.info("EventBus成功创建");
    }

    public EventBus(int loopNum) {
        this.eventRegistry = new HashMap<>();
        this.executors = new NioEventLoopGroup(loopNum);
        LOG.info("EventBus成功创建");
    }

    public static class Listener {
        Object obj;
        String funcName;

        Listener(Object listener, String funcName) {
            this.obj = listener;
            this.funcName = funcName;
        }
    }

    public List<Future<Object>> asyncFireEvent(String event, Object ...args) {
        List<Listener> listenerList = this.eventRegistry.get(event);
        if (listenerList == null) {
            return null;
        }
        List<Future<Object>> futureList = new ArrayList<>();
        if (executors == null) {
            this.executors = armOT.executors;
        }
        for (Listener listener : listenerList) {
            Future<Object> future = executors.submit(new EventListenerThread(listener, args));
            futureList.add(future);
        }
        return futureList;
    }

    public void asyncEventTriggered() {
        //todo 事件完成
    }

    private static class EventListenerThread implements Callable<Object>{
        Listener listener;
        Object[] args;

        public EventListenerThread(Listener listener, Object ...args) {
            this.listener = listener;
            this.args = args;
        }

        @Override
        public Object call() throws Exception {
            Class<?> clazz = listener.obj.getClass();
            String funcName = listener.funcName;
            Class<?>[] argTypes = new Class<?>[args.length];
            int var1 = 0;
            for (Object arg : args) {
                argTypes[var1++] = args[var1].getClass();
            }
            Method method = clazz.getDeclaredMethod(funcName, argTypes);
            method.setAccessible(true);
            return method.invoke(listener.obj, args);
        }
    }

    public void registerEvent(Object listener, String event, String funcName) {
        Listener func = new Listener(listener, funcName);
        List<Listener> funcList = this.eventRegistry.getOrDefault(event, new ArrayList<>());
        funcList.add(func);
        this.eventRegistry.put(event, funcList);
    }
}
