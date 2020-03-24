package structure;

import config.Const;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:38
 */

public class ModuleConfig {

    private String moduleName;

    public ModuleConfig(String packagePath) {
        //todo 从配置文件中读模块（程序）位置
        this.moduleName = packagePath;
    }

    public String getModuleName() {
        return moduleName;
    }
}
