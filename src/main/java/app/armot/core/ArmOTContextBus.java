package app.armot.core;


import app.armot.api.bean.Device;
import core.Injector;

import java.util.Random;

/**
 * @author Cai2yy
 * @date 2020/2/26 12:57
 */

public class ArmOTContextBus {

    public static void initNewContext() {
        Injector injector = new Injector();
        ArmOT armOT = injector.getInstance(ArmOT.class).asyncInit();
        System.out.println(new Random().nextInt() + " 初始化新连接...");
        armOT.getDevices().put(0, new Device("XiaomiSwitch", 0));
    }

    private static void registerAssemble() {

    }

}
