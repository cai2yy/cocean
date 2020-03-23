package core;

import config.Const;
import loader.ModuleLoader;
import structure.ApplicationConfig;
import structure.Module;
import structure.ModuleConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cai2yy
 * @date 2020/3/22 8:51
 */

public class Container {

    Map<String, Module> moduleMap ;

    ModuleLoader moduleLoader;

    public Container(ApplicationConfig config) {
        String moduleName = config.getModuleName();
        this.moduleMap = new ConcurrentHashMap<>();
        System.out.println("生成新的");
        this.moduleLoader = new ModuleLoader();

        //从配置文件读
        ModuleConfig moduleConfig = new ModuleConfig(Const.modulePath1);

        this.moduleMap.put(moduleName, moduleLoader.load(moduleConfig));
        initHotDeploy();
    }

    /**
     * 启动一个线程负责热加载
     * 该线程隔一定时间查看模块路径下的文件是否被修改，若是，则进行热重启
     * @param
     * @return
     */
    public void initHotDeploy() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Module module = null;
                    for (String moduleName : moduleMap.keySet()) {
                        module = moduleMap.get(moduleName);
                        // 如果模块程序文件被更新，则重加载模块
                        if (moduleLoader.checkModified(module)) {
                            module = moduleLoader.load(module.getModuleConfig());
                            moduleMap.put(moduleName, module);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /*
    public void refresh() {
        synchronized(this) {
            this.prepareRefresh();
            // 获取新的BeanFactory，销毁原有BeanFactory
            ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
            // 为BeanFactory进行必要的准备工作
            this.prepareBeanFactory(beanFactory);

            try {
                // 额外后处理
                this.postProcessBeanFactory(beanFactory);
                // 执行BeanFactoryPostProcessor的回调
                this.invokeBeanFactoryPostProcessors(beanFactory);
                // 注册所有BeanPostProcessor
                this.registerBeanPostProcessors(beanFactory);
                // 初始化信息资源
                this.initMessageSource();
                // 初始化时间多播器
                this.initApplicationEventMulticaster();
                // 正在刷新
                this.onRefresh();
                // 注册所有ApplicationListener
                this.registerListeners();
                // 完成BeanFactory的初始化流程
                this.finishBeanFactoryInitialization(beanFactory);
                // 完成刷新
                this.finishRefresh();
            } catch (BeansException var9) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
                }
                // 若失败则清理资源
                this.destroyBeans();
                this.cancelRefresh(var9);
                throw var9;
            } finally {
                this.resetCommonCaches();
            }

        }
    }

    protected final void refreshBeanFactory() throws BeansException {
        // 如果有Bean先销毁
        if (this.hasBeanFactory()) {
            this.destroyBeans();
            this.closeBeanFactory();
        }

        try {
            DefaultListableBeanFactory beanFactory = this.createBeanFactory();
            beanFactory.setSerializationId(this.getId());
            this.customizeBeanFactory(beanFactory);
            this.loadBeanDefinitions(beanFactory);
            synchronized(this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        } catch (IOException var5) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + this.getDisplayName(), var5);
        }
    }

     */

}
