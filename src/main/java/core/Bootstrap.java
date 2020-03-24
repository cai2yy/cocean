package core;

import structure.ApplicationConfig;

/**
 * @author Cai2yy
 * @date 2020/3/22 12:44
 */

public class Bootstrap {

    public static void main(String[] args) {
        // 初始化主加载器
        Injector majorInjector = new Injector();
        // 初始化事件总线
        EventBus eventBus = majorInjector.getInstance(EventBus.class);
        eventBus.registerEvent(new EventBusTest(), "happen", "say");
        // 初始化外部容器
        Container container = majorInjector.getInstance(Container.class);
        // 初始化程序配置文件
        ApplicationConfig applicationConfig = majorInjector.getInstance(ApplicationConfig.class);
        applicationConfig.initModuleConfigs();
        // 测试
        applicationConfig.setTest(222);
        eventBus.setApplicationConfig(applicationConfig);
        container.setApplicationConfig(applicationConfig);

        /* 为外部事件总线注册事件
        eventBus.registerEvent(new EventBusTest(), "happen", "happen");
         */

        container.initModules();
        container.initHotDeploy();
    }


}
