package context;

import core.Injector;
import structure.Module;
import test.circle.A;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Cai2yy
 * @date 2020/3/21 17:08
 */

public class ModuleApplicationContext extends AbstractApplicationContext {

    Module module;

    Injector injector;

    public ModuleApplicationContext(Injector injector) {
        this.injector = injector;
    }

    /**
     * 刷新整个模块（程序），从当前类加载器重载对应类
     * @param
     * @return
     */
    public void refresh() {
        stop();
        start();
    }

    public void stop() {

    }

    /**
     * 初始化各个组件
     * @param
     * @return
     */
    public void start() {
        Collection<Class<?>> collection = injector.getCells();
        Class<? extends Annotation> initAnno = null;
        Class<? extends Annotation> lazyAnno = null;
        try {
            initAnno = (Class<? extends Annotation>) injector.getClassLoader().loadClass("annotations.Init");
            lazyAnno = (Class<? extends Annotation>) injector.getClassLoader().loadClass("annotations.Lazy");
        } catch (Exception e) {
            // ignored
        }
        for (Class<?> clazz : collection) {
            // 如果@Lazy懒加载则忽略
            if (clazz.isAnnotationPresent(lazyAnno)) {
                continue;
            }
            // 反射激活带有@Init标签的初始化方法
            Method[] methods = clazz.getDeclaredMethods();
            Method method0 = null;
            for (Method method : methods) {
                if (method.isAnnotationPresent(initAnno)) {
                    method0 = method;
                    break;
                }
            }
            try {
                method0.invoke(injector.getInstance(clazz));
            } catch (Exception e) {
                // ignored
            }
        }
    }

}
