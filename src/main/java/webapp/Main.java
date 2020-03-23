package webapp;

import annotations.Init;
import annotations.Inject;
import annotations.Singleton;
import test.mvc.service.UserService;

import java.util.Timer;

/**
 * @author Cai2yy
 * @date 2020/3/22 15:32
 */

@Singleton
public class Main {

    @Inject
    private Service userService;

    @Init
    public void init() {
        System.out.println("253个webApp启动了" + System.nanoTime());
        userService.addUser();
    }

    public void handler() {
        System.out.println("接收到了一个网络服务，并做出了响应");
    }

}
