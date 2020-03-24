package utils;

import loader.ModuleClassLoader;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Cai2yy
 * @date 2020/2/22 13:06
 */

public class ScannerTest {

    public static void main(String[] args) throws ClassNotFoundException, MalformedURLException {
        URL url1 = Thread.currentThread().getContextClassLoader().getResource("");
        String path = url1.getPath();
        String jarPath = Scanner.getClassName("D:/CS/PersonalProj/cocean/target/lib").get(0);
        URL url2 = new URL("file:/" + jarPath);
        System.out.println("url1 " + url1.getPath());
        System.out.println("url2: " + url2.getPath());
        List<String> jarNames = Scanner.getClassNameByJar(jarPath, true);
        jarNames.remove(0);

        ClassLoader classLoader = new ModuleClassLoader(new URL[]{url1, url2}, null);
        classLoader.loadClass("webapp.Main");
        classLoader.loadClass("webapp.NettySuccessor");

    }
}
