package app.armot.mock;

import annotations.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cai2yy
 * @date 2020/2/23 23:23
 */
@Singleton
public class UserDB {

    private Map<String, String> rows = new HashMap<>();
    {
        rows.put("cai2yy", "cai2yy");
        rows.put("admin", "admin");
    }

    public boolean checkAccess(String name, String passwd) {
        return rows.containsKey(name) && rows.get(name).equals(passwd);
    }

}
