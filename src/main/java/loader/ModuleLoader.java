package loader;

import context.ApplicationContext;
import context.ModuleApplicationContext;
import core.Injector;
import structure.Module;
import structure.ModuleConfig;
import utils.Scanner;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:38
 */

public class ModuleLoader implements Loader {

    ApplicationContext applicationContext;

    Map<String, Long> classModifiedMap;

    @Override
    public boolean checkModified(Module module) {
        String path = module.getModulePath();
        path = Thread.currentThread().getContextClassLoader().getResource(path).getPath();
        // 第一次检查，初始化 类-修改时间 缓存
        if (classModifiedMap == null) {
            classModifiedMap = Scanner.getClassModifiedTime(path, null, true);
        }
        // 检查模块文件有没有被修改过
        else {
            Map<String, Long> tmpMap = Scanner.getClassModifiedTime(path, null, true);
            System.out.println(tmpMap.size() + " vs " + classModifiedMap.size());
            if (tmpMap.size() != classModifiedMap.size()) {
                // 类的数量不相同
                this.classModifiedMap = tmpMap;
                return true;
            }
            for (String key : tmpMap.keySet()) {
                if (!classModifiedMap.containsKey(key)) {
                    // 类的数量不变，但有新增有移除
                    this.classModifiedMap = tmpMap;
                    return true;
                }
                if (!classModifiedMap.get(key).equals(tmpMap.get(key))) {
                    // 类的数量不变，有些类被更新了
                    this.classModifiedMap = tmpMap;
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
    public Module load(ModuleConfig moduleConfig) {
        String tmpFileURLs = moduleConfig.getModuleUrlPath();

        ApplicationContext applicationContext = loadModuleApplication(moduleConfig, tmpFileURLs);

        return new Module(applicationContext, moduleConfig);

    }


    /**
     * 热加载模块（程序）的执行方法
     * 为实现热加载，必须破坏双亲委派机制，切换成新创建的自定义类加载器来加载（而不是AppCL）
     * @param
     * @return
     */
    private ModuleApplicationContext loadModuleApplication(ModuleConfig moduleConfig, String FileURLs) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        // 创建新的模块加载器
        Injector injector = new Injector(FileURLs);
        ClassLoader moduleClassLoader = injector.getClassLoader();
        // 切换到模块类加载器
        Thread.currentThread().setContextClassLoader(moduleClassLoader);
        // 重启模块（程序））
        ModuleApplicationContext moduleApplicationContext = new ModuleApplicationContext(injector);
        moduleApplicationContext.refresh();
        // 切换回原来的类加载器
        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return moduleApplicationContext;

    }

}
