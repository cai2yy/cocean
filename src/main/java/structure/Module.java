package structure;

import context.ApplicationContext;
import context.ModuleApplicationContext;
import core.Injector;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:37
 */

public class Module {

    ModuleConfig moduleConfig;

    Injector injector;

    public String getModulePath() {
        return moduleConfig.getModuleUrlPath();
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public Module(ApplicationContext context, ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public Injector getInjector() {
        return injector;
    }

}
