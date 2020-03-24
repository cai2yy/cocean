package structure;

import context.ApplicationContext;
import core.Injector;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:37
 */

public class Module {

    ModuleConfig moduleConfig;

    Injector injector;

    public String getModulePath() {
        return moduleConfig.getModuleName();
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public Module(ApplicationContext applicationContext, ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public Injector getInjector() {
        return injector;
    }

}
