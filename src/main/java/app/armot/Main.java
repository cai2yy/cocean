package app.armot;

import annotations.Init;
import annotations.Inject;
import annotations.Singleton;
import app.armot.api.controller.DeviceController;
import app.armot.api.controller.RPCController;
import app.armot.core.ArmOT;
import app.armot.server.RequestDispatcher;
import app.armot.server.Router;
import app.armot.server.internal.HttpServer;
import app.armot.utils.MethodInterTest;
import core.EventBus;
import core.EventBusHelper;
import loader.ModuleClassLoader;
import loader.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Cai2yy
 * @date 2020/3/24 16:36
 */

@Singleton
public class Main {

    @Inject
    ArmOT armOT;

    @Inject
    DeviceController deviceController;

    @Inject
    RPCController rpcController;


    @Init
    public void init() {
        /*
        测试子容器与外部容器的通信1
        public void init(Map messenger){
        System.out.println(messenger.getClass());
        Map<String, ClassLoader> classLoaderMap = (Map<String, ClassLoader>) messenger;
        for (String key : classLoaderMap.keySet()) {
            System.out.println(key + " : " + classLoaderMap.get(key));
        }

        测试子容器与外部容器的通信2
        public void init(Class eventBus, Object eventBusObj){

        EventBusHelper eventBusHelper = new EventBusHelper(eventBus, eventBusObj);
        // 传入时间key，和1个 or 多个参数
        eventBusHelper.fireEvent("happen", "通信成功了");
         */

        /** 1. (本地) 初始化必要组件 */
        System.out.println("创建ArmOT6666666" + Thread.currentThread().getContextClassLoader());
        armOT.asyncInit();
        System.out.println("主程序异步运行");
        //armOT.getDevices().put(0, new Device("XiaomiSwitch", 0));
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        /** 2. 启动web服务 */
        // 构建路由，添加静态资源目录和子路由
        var router = new Router((ctx, req) -> {
            //todo 此处和Controller里的修改似乎无法热重启，但本页上面却可以，似乎是IDEA或netty的缓存问题
            ctx.html("Hello, World~~5");})
                .resource("/pub", "/static")
                .child("/device", deviceController)
                .child("/rpc", rpcController);
        System.out.println(deviceController.getClass().getClassLoader());
        // 初始化分发器，设定根url路径
        var rd = new RequestDispatcher("armot", router)
                .templateRoot("/tpl");

        var server = new HttpServer("localhost", 8080, 2,18, rd);
        // 运行
        threadPool.execute(server::start);
        // 优雅关闭,为JVM添加关机钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                server.stop();
            }

        });

    }


    public void handler() {
        System.out.println("接收到了一个网络服务，并做出了响应");
    }

}
