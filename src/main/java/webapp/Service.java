package webapp;

import annotations.Inject;
import annotations.Named;
import annotations.Singleton;
import test.mvc.dao.UserRepository;

/**
 * @author Cai2yy
 * @date 2020/3/23 17:23
 */

@Singleton
@Named("service")
public class Service {

    @Inject
    @Singleton
    @Named("dao1")
    private Mapper userMapper;

    @Inject
    public Service() {
        System.out.println("userService构造器");
    }

    public void addUser() {
        userMapper.add();
        System.out.println("Service层完成操作");
    }

}
