package context;

/**
 * @author Cai2yy
 * @date 2020/3/21 16:44
 */

public interface ApplicationContext {

    String getId();

    String getApplicationName();

    long getStartupTime();

    ApplicationContext getParent();

}
