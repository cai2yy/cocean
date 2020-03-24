package app.armot;


import core.Injector;
import app.armot.server.Router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Cai2yy
 * @date 2020/2/21 19:16
 */

public class Application {

    /**
     *  项目启动函数
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        /** 1. (本地) 初始化必要组件 */
        Injector injector = new Injector("app.armot");
        System.out.println("创建injector完成");

        // 反射调用
        Class armOTClazz = injector.classLoader.loadClass("app.armot.core.ArmOT");
        Object armOT = injector.getInstance(armOTClazz);
        Method asyncInitMethod = armOT.getClass().getMethod("asyncInit");
        asyncInitMethod.invoke(armOT);

        System.out.println("主程序异步运行");
        //armOT.getDevices().put(0, new Device("XiaomiSwitch", 0));
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        /** 2. 启动web服务 */
        // 构建路由，添加静态资源目录和子路由
        Class<?> routerClazz = injector.classLoader.loadClass("app.armot.server.Router");
        Object router = routerClazz.getConstructor().newInstance(null);
        Router x = new Router();

        Class<?> devicesControllerClazz = injector.classLoader.loadClass("app.armot.api.controller.DeviceController");
        Class<?> controllerClazz = injector.classLoader.loadClass("app.armot.server.Controller");
        Method childMethod = routerClazz.getMethod("child", String.class, controllerClazz);
        childMethod.invoke(router, "/device", injector.getInstance(devicesControllerClazz));

        // 初始化分发器，设定根url路径
        Class<?> requestDispatcherClazz = injector.classLoader.loadClass("app.armot.server.RequestDispatcher");
        Class<?> iRequestDispatcherClazz = injector.classLoader.loadClass("app.armot.server.internal.IRequestDispatcher");
        Object rd = requestDispatcherClazz.getConstructor(String.class, routerClazz).newInstance("armot", router);
        //var rd = new RequestDispatcher("armot", router).templateRoot("/tpl");

        Class<?> httpClazz = injector.classLoader.loadClass("app.armot.server.internal.HttpServer");
        System.out.println("开始创建HttpServer实例！");
        Object server = httpClazz.getConstructor(String.class, int.class, int.class, int.class, iRequestDispatcherClazz)
                .newInstance("localhost", 8080, 2,18, rd);
        // 运行
        Method startMethod = httpClazz.getMethod("start");
        // todo 研究反射+异步任务如何处理
        // 此处无法封装成runnable
        System.out.println("服务器启动！");
        startMethod.invoke(server);
        System.out.println("服务器启动成功！");
        // 优雅关闭,为JVM添加关机钩子

    }


}
