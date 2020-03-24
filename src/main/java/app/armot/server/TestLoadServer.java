package app.armot.server;

import loader.ModuleClassLoader;
import utils.Scanner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Cai2yy
 * @date 2020/3/24 14:25
 */

public class TestLoadServer {

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {

        // 模块根目录
        URL url1 = Thread.currentThread().getContextClassLoader().getResource("");

        // 模块依赖jar包
        List<String> jarPaths = Scanner.getClassName("D:/CS/PersonalProj/cocean/target/lib");
        URL[] urls = new URL[jarPaths.size() + 1];
        int var1 = 0;
        urls[var1++] = url1;
        for (String jarPath : jarPaths) {
            urls[var1++] = new URL("file:/" + jarPath);
        }

        // 初始化类加载器
        ClassLoader classLoader = new ModuleClassLoader(urls, null);

        List<String> classesName = Scanner.getClassName("");
        for (String className : classesName) {
            classLoader.loadClass(className);
        }


    }
}
