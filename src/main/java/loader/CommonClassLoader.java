package loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Cai2yy
 * @date 2020/3/22 8:47
 */

public class CommonClassLoader extends URLClassLoader {

    public CommonClassLoader(URL[] urls) {
        super(urls);
    }
}
