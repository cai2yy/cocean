package app.armot;


import app.armot.core.ArmOT;
import core.Injector;

/**
 * @author Cai2yy
 * @date 2020/2/26 12:30
 */

public class test {

    public static void main(String[] args) {
        String path = "app.armot";
        System.out.println(path);
        path = Thread.currentThread().getContextClassLoader().getResource(path).getPath();
    }
}
