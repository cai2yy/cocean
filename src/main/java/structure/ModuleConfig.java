package structure;

import java.net.URL;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:38
 */

public class ModuleConfig {

    private String ModulePath;

    public ModuleConfig(String packagePath) {
        //todo 从配置文件中读模块（程序）位置
        this.ModulePath = packagePath;
    }

    public String getModuleUrlPath() {
        return ModulePath;
    }
}
