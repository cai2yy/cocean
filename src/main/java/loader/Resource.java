package loader;

/**
 * @author Cai2yy
 * @date 2020/3/22 23:00
 */

public class Resource {

    public long lastModified = -1;

    public volatile Class<?> loadedClass;

    Resource(Class<?> loadedClass) {
        this.loadedClass = loadedClass;
    }

}
