package context;

/**
 * @author Cai2yy
 * @date 2020/3/21 17:16
 */

public class AbstractApplicationContext implements ApplicationContext {


    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public long getStartupTime() {
        return 0;
    }

    @Override
    public ApplicationContext getParent() {
        return null;
    }
}
