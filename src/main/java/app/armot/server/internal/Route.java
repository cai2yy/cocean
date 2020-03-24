package app.armot.server.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Cai2yy
 * @date 2020/2/21 23:41
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String url() default "";
    String methods() default "GET";
}
