package utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Cai2yy
 * @date 2020/2/20 23:23
 */

public class Scanner {

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<Class<?>> getClasses(String packageName) {
        return getClasses(packageName, true);
    }

    public static List<Class<?>> getClasses() {
        String packageName = "";
        return getClasses(packageName, true);
    }

    public static List<Class<?>> getClasses(String packageName, boolean childPackage) {
        List<String> classNames = getClassName(packageName, childPackage);
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        return classes;
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     * @param packageName 包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNames = getClassNameByFile(url.getPath(), null, childPackage);
            } else if (type.equals("jar")) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            // 可能是包路径,按文件处理
            //fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
            fileNames = getClassNameByFile(packagePath, null, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * @param filePath 文件路径
     * @param className 类名集合
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles == null) {
            return null;
        }
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
                else if (childFilePath.endsWith(".jar")) {
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 获取该文件目录及其子目录下.class文件的修改时间
     * @param filePath 文件路径
     * @param modifiedMap 文件修改时间缓存，递归用
     * @param childPackage 是否遍历子包
     * @return 文件修改时间的缓存哈希表
     */
    public static Map<String, Long> getClassModifiedTime(String filePath, Map<String, Long> modifiedMap, boolean childPackage) {
        Map<String, Long> classModifiedMap = new ConcurrentHashMap<>();

        // 扫描路径下所有文件
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles == null) {
            return null;
        }
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                // 递归查找子目录
                if (childPackage) {
                    Map<String, Long> tmpMap = getClassModifiedTime(childFile.getPath(), classModifiedMap, childPackage);
                    for (String name : tmpMap.keySet()) {
                        classModifiedMap.put(name, tmpMap.get(name));
                    }
                }
            } else {
                String childFilePath = childFile.getPath();
                if (!(childFile.getPath().endsWith(".class") || childFilePath.endsWith(".jar"))) {
                    continue;
                }
                // 把文件最后修改时间加入结果集
                classModifiedMap.put(childFilePath, childFile.lastModified());
            }
        }
        return classModifiedMap;
    }


    /**
     * 从jar获取某包下所有类
     * @param jarPath jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        //String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String jarFilePath = jarPath;
        //String packagePath = jarInfo[1].substring(1);
        String packagePath = jarPath;
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        //if (entryName.startsWith(packagePath)) {
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        myClassName.add(entryName);
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     * @param urls URL集合
     * @param packagePath 包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}
