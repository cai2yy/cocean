package structure;


import annotations.Inject;
import app.armot.Application;
import config.Const;
import context.ModuleApplicationContext;
import core.Container;
import core.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cai2yy
 * @date 2020/3/22 12:45
 */

public class ApplicationConfig {

    @Inject
    EventBus eventBus;

    @Inject
    Container container;

    //todo 测试，稍后清除
    int test;
    public int getTest() {
        return test;
    }
    public void setTest(int test) {
        this.test = test;
    }

    List<ModuleApplicationContext> moduleApplicationContexts = new ArrayList<>();

    public void addModuleApplicationContext(ModuleApplicationContext moduleApplicationContext) {
        this.moduleApplicationContexts.add(moduleApplicationContext);
    }

    public Module getModule(String name) {
        for (ModuleApplicationContext moduleApplicationContext : moduleApplicationContexts) {
            if (name.equals(moduleApplicationContext.getModule().getModuleName())) {
                return moduleApplicationContext.getModule();
            }
        }
        return null;
    }

    public Map<String, ClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    public void addClassLoader(String name, ClassLoader classLoader) {
        classLoaderMap.put(name, classLoader);
    }

    public Map<String, ClassLoader> getClassLoader() {
        return classLoaderMap;
    }

    List<ModuleConfig> moduleConfigs = new ArrayList<>();

    public void initModuleConfigs() {
        String module1Name = "armot";
        String module2Name = "webapp";
        //从配置文件读
        ModuleConfig moduleConfig = new ModuleConfig(Const.modulePath1);
        ModuleConfig moduleConfig2 = new ModuleConfig(Const.modulePath2);

        List<ModuleConfig> res = new ArrayList<>();
        res.add(moduleConfig);
        res.add(moduleConfig2);
        this.moduleConfigs = res;
    }

    public List<ModuleConfig> getModuleConfigs() {
        return moduleConfigs;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
