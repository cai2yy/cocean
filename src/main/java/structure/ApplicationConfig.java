package structure;


import annotations.Inject;
import config.Const;
import core.Container;
import core.EventBus;
import core.ModuleLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

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

    List<ModuleConfig> moduleConfigList = new ArrayList<>();

    public void initModuleConfigs() {
        String module1Name = "armot";
        String module2Name = "webapp";
        //从配置文件读
        ModuleConfig moduleConfig = new ModuleConfig(Const.modulePath1);
        ModuleConfig moduleConfig2 = new ModuleConfig(Const.modulePath2);

        List<ModuleConfig> res = new ArrayList<>();
        res.add(moduleConfig);
        res.add(moduleConfig2);
        this.moduleConfigList = res;
    }

    public List<ModuleConfig> getModuleConfigList() {
        return moduleConfigList;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
