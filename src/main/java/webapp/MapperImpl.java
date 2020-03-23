package webapp;

import annotations.Named;
import annotations.Singleton;

/**
 * @author Cai2yy
 * @date 2020/3/23 17:23
 */
@Singleton
@Named("dao1")
public class MapperImpl implements Mapper {

    public void add() {
        System.out.println("I am UserMapper1");
        System.out.println("在数据库的用户表中插入一条数据");
    }

    public void print() {
        System.out.println("I am UserMapper1");
    }
}
