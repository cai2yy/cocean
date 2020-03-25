package core;

import annotations.Inject;
import context.ModuleApplicationContext;
import loader.ModuleClassLoader;
import structure.ApplicationConfig;
import structure.Module;
import structure.ModuleConfig;
import utils.Scanner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:38
 */

public class ModuleLoader implements Loader {

    @Inject
    EventBus eventBus;

    // 懒加载
    ApplicationConfig applicationConfig;

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    Map<String, Map<String, Long>> moduleClassModifiedMap;

    /**
     * 初始化外部容器，加载所有模块
     * @param
     * @return
     */
    public Map<String, Module> loadModules() {
        System.out.println("初始化，加载所有模块");
        Map<String, Module> modules = new ConcurrentHashMap<>();

        List<ModuleConfig> moduleConfigs = applicationConfig.getModuleConfigs();
        for (ModuleConfig moduleConfig : moduleConfigs) {
            modules.put(moduleConfig.getModuleName(), load(moduleConfig).getModule());
        }
        return modules;
    }

    @Override
    public boolean checkModified(String moduleName, Module module) {
        String path = module.getModulePath();
        path = Thread.currentThread().getContextClassLoader().getResource(path).getPath();
        // 第一次检查，初始化 类-修改时间 缓存
        if (moduleClassModifiedMap == null) {
            this.moduleClassModifiedMap = new ConcurrentHashMap<>();
        }
        if (!moduleClassModifiedMap.containsKey(moduleName)) {
            moduleClassModifiedMap.put(moduleName, Scanner.getClassModifiedTime(path, null, true));
        }
        // 检查模块文件有没有被修改过
        else {
            Map<String, Long> classModifiedMap = moduleClassModifiedMap.get(moduleName);
            Map<String, Long> tmpMap = Scanner.getClassModifiedTime(path, null, true);
            if (tmpMap.size() != classModifiedMap.size()) {
                // 类的数量不相同
                this.moduleClassModifiedMap.put(moduleName, tmpMap);
                return true;
            }
            for (String key : tmpMap.keySet()) {
                if (!classModifiedMap.containsKey(key)) {
                    // 类的数量不变，但有新增有移除
                    this.moduleClassModifiedMap.put(moduleName, tmpMap);
                    return true;
                }
                if (!classModifiedMap.get(key).equals(tmpMap.get(key))) {
                    // 类的数量不变，有些类被更新了
                    this.moduleClassModifiedMap.put(moduleName, tmpMap);
                    return true;
                }
            }
        }
        // 没有被修改过
        return false;

    }

    /**
     * 热加载模块（程序）
     * @param
     * @return
     */
    @Override
    public ModuleApplicationContext load(ModuleConfig moduleConfig) {
        System.out.println("加载模块: " + moduleConfig.getModuleName());
        String tmpFileURLs = moduleConfig.getModuleName();

        ModuleApplicationContext moduleApplicationContext = loadModuleApplication(moduleConfig, tmpFileURLs);

        Module module = new Module(moduleApplicationContext, moduleConfig);
        moduleApplicationContext.setModule(module);

        return moduleApplicationContext;

    }


    /**
     * 热加载模块（程序）的执行方法
     * 为实现热加载，必须破坏双亲委派机制，切换成新创建的自定义类加载器来加载（而不是AppCL）
     * @param
     * @return
     */
    private ModuleApplicationContext loadModuleApplication(ModuleConfig moduleConfig, String FileURLs) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        // 创建新的子容器
        Injector injector = new Injector(FileURLs);

        // 获取子容器对应的类加载器
        ModuleClassLoader moduleClassLoader = (ModuleClassLoader) injector.getClassLoader();
        // 注册类加载器到事件总线
        moduleClassLoader.setEventBus(eventBus);
        eventBus.addClassLoader(moduleConfig.getModuleName(), moduleClassLoader);

        // 切换到模块类加载器
        Thread.currentThread().setContextClassLoader(moduleClassLoader);


        // 重启模块（程序））
        ModuleApplicationContext moduleApplicationContext = new ModuleApplicationContext(injector);
        moduleApplicationContext.setApplicationConfig(applicationConfig);
        moduleApplicationContext.setEventBus(eventBus);
        //System.out.println("放入了一个模块" + moduleApplicationContext);
        applicationConfig.addModuleApplicationContext(moduleApplicationContext);
        applicationConfig.addClassLoader(moduleConfig.getModuleName(), moduleClassLoader);
        moduleApplicationContext.refresh();

        // 切换回原来的类加载器
        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return moduleApplicationContext;

    }

}
