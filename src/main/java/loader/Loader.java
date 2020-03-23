package loader;

import core.Injector;
import structure.Module;
import structure.ModuleConfig;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:36
 */

public interface Loader {

    public boolean checkModified(Module module);

    public Module load(ModuleConfig moduleConfig);

}
