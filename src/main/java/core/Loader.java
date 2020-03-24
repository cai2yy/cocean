package core;

import context.ModuleApplicationContext;
import core.Injector;
import structure.Module;
import structure.ModuleConfig;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:36
 */

public interface Loader {

    public boolean checkModified(String moduleName, Module module);

    public ModuleApplicationContext load(ModuleConfig moduleConfig);

}
