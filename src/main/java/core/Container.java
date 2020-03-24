package core;

import annotations.Inject;
import app.armot.Application;
import config.Const;
import structure.ApplicationConfig;
import structure.Module;
import structure.ModuleConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Cai2yy
 * @date 2020/3/22 8:51
 */

public class Container {

    @Inject
    ModuleLoader moduleLoader;

    // 懒加载
    Map<String, Module> moduleMap;

    ApplicationConfig applicationConfig;

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
        this.moduleLoader.setApplicationConfig(applicationConfig);
    }

    // 构造器初始化
    ExecutorService threadPoolExecutor;


    /**
     * 初始化外部容器
     * @param
     * @return
     */
    public Container() {
        this.threadPoolExecutor = Executors.newFixedThreadPool(18);
        /*
        String moduleName = applicationConfig.getModuleName();
        this.moduleMap = new ConcurrentHashMap<>();
        this.moduleLoader = new ModuleLoader();
        this.threadPoolExecutor = Executors.newFixedThreadPool(10);
        //从配置文件读
        ModuleConfig moduleConfig = new ModuleConfig(Const.modulePath1);
        ModuleConfig moduleConfig2 = new ModuleConfig(Const.modulePath2);
        String module2Name = "webapp";

        this.moduleMap.put(moduleName, moduleLoader.load(moduleConfig));
        this.moduleMap.put(module2Name, moduleLoader.load(moduleConfig2));

         */

    }

    public void initModules() {
        this.moduleMap = moduleLoader.loadModules();
    }

    /**
     * 启动一个线程负责热加载
     * 该线程隔一定时间查看模块路径下的文件是否被修改，若是，则进行热重启
     * @param
     * @return
     */
    public void initHotDeploy() {

        // 让一个线程循环监听是否有模块发生变化
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Module module = null;
                    System.out.println("有几个程序：" + moduleMap.size());
                    for (String moduleName : moduleMap.keySet()) {
                        module = moduleMap.get(moduleName);
                        // 如果模块程序文件被更新，则重加载模块
                        if (moduleLoader.checkModified(moduleName, module)) {
                            System.out.println(moduleName + "这个程序被更改过");
                            Module finalModule = module;
                            threadPoolExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    moduleMap.put(moduleName, moduleLoader.load(finalModule.getModuleConfig()).getModule());
                                }
                            });
                        }
                    }
                }
            }
        });
    }


}
