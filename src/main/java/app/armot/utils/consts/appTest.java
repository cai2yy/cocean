package app.armot.utils.consts;

import annotations.Inject;
import core.Injector;

/**
 * @author Cai2yy
 * @date 2020/3/24 15:33
 */

public class appTest {

    public static void main(String[] args) {
        Injector injector = new Injector("webapp");
        System.out.println("创建injector完成");
        System.out.println(Injector.getInjector());
    }
}
