package loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模块类加载器，用来容器化模块（程序）
 * @author Cai2yy
 * @date 2020/3/21 16:36
 */
public class ModuleClassLoader extends URLClassLoader {

    ClassLoader JavaSystemClassLoader = getPlatformClassLoader();

    private String classPath;
    private String libPath;

    protected final Map<String, Resource> resourceEntries =
            new ConcurrentHashMap<>();

    private final static Logger LOGGER = LoggerFactory.getLogger(ModuleClassLoader.class);

    public ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.classPath = urls[0].getPath().substring(1);
        this.libPath = classPath.substring(0, classPath.lastIndexOf("target") + 6).concat("/lib/");
    }

    public ModuleClassLoader(URL[] urls, ClassLoader parent, String classPath) {
        super(urls, parent);
        System.out.println("初始化类加载器URL目录" + urls[0].getPath());
        this.classPath = classPath;
        this.libPath = classPath.substring(0, classPath.lastIndexOf("target") + 6).concat("/lib/");
    }

    @Override
    public String getName() {
        return "ModuleClassLoader";
    }

    /**
     * 加载类的外层实现，重写该方法可能破坏双亲委派模型
     * @param
     * @return
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;

        //todo 研究此处如何加锁
        synchronized(ModuleClassLoader.class) {
            // (0) 不重复加载
            if (this.isEligibleForOverriding(name)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("覆盖加载该类: {}", name);
                }
                clazz = this.loadClassForOverriding(name);
            }

            if (clazz != null) {
                if (resolve) {
                    this.resolveClass(clazz);
                }

                return clazz;
            }

            // (0.5) 查找是否已加载过
            clazz = findLoadedClass(name);
            if (clazz != null) {
                System.out.println(name  + "已经被加载过了");
                return clazz;
            }
            else {
            }

            // (1) 首先用java系统加载器加载
            try {
                clazz = JavaSystemClassLoader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }

            // (2) 尝试自己加载
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("  Searching local repositories");
            try {
                // 这一步有时候（在初始化，加载第一个类时）会卡很久
                clazz = findClass(name);
                if (clazz != null) {
                    System.out.println("自己加载成功>> " + clazz);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("  Loading class from local repository");
                    if (resolve)
                        resolveClass(clazz);
                    resourceEntries.put(binaryNameToPath(name, true), new Resource(clazz));
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }

            // (3) 无法加载，委托给双亲加载器
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("  Delegating to parent classloader at end: " + getParent().getName());
            try {
                clazz = Class.forName(name, false, getParent());
                if (clazz != null) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        //无法加载，可能是由于加载的jar包内部类依赖于maven管理的外部类
        return null;
    }


    /**
     * 重写了加载类的底层方法
     * @param
     * @return
     */
    /*
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = null;
            if (name.startsWith("fromJar:")) {
                path = libPath.replace("/", "\\\\") + name.substring(8).replaceAll("\\.", "\\\\") + ".class" ;
            }
            else {
                path = classPath.replace("/", "\\\\") + name.replaceAll("\\.", "\\\\") + ".class" ;

            }
            FileInputStream in = new FileInputStream(path) ;
            ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
            byte[] buf = new byte[1024] ;
            int len = -1 ;
            while((len = in.read(buf)) != -1){
                baos.write(buf , 0 , len);
            }
            in.close();
            byte[] classBytes = baos.toByteArray();
            //System.out.println("-->加载成功" + classPath  + name);
            return defineClass(classBytes, 0, classBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }

     */

    /**
     * 重新（final方法不可重写）另写了个搜索已加载缓存类的方法，使用哈希表做缓存
     * @param
     * @return
     */
    protected Class<?> findLoadedClass0(String name) {

        String path = binaryNameToPath(name, true);

        Resource entry = resourceEntries.get(path);
        if (entry != null) {
            return entry.loadedClass;
        }
        return null;
    }

    /**
     * 路径转换
     * @param
     * @return
     */
    private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
        // 1 for leading '/', 6 for ".class"
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        if (withLeadingSlash) {
            path.append('/');
        }
        path.append(binaryName.replace('.', '/'));
        String CLASS_FILE_SUFFIX = ".class";
        path.append(CLASS_FILE_SUFFIX);
        return path.toString();
    }


    public boolean isLoaded(String name) {
        return findLoadedClass(name) != null;
    }

    private boolean isEligibleForOverriding(String name) {
        //todo 未被修改的类
        return false;
    }

    /**
     * 避免重复加载
     * @param
     * @return
     */
    private Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
        Class<?> result = this.findLoadedClass(name);
        if (result == null) {
            // findClass()为加载类的底层实现方法
            result = this.findClass(name);
        }

        return result;
    }

    private String getLastModifiedTime(String path) {
        File f = new File(path);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(f.lastModified());
        return sdf.format(cal.getTime());
    }


}
