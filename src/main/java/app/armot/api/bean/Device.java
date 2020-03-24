package app.armot.api.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cai2yy
 * @date 2020/2/21 19:35
 */

public class Device {

    int id;

    Map<String, String> data;

    public Device(String type, int id) {
        data = new HashMap<>(5);
        data.put("type", type);
        this.id = id;
    }

    public Device(String type, String name, int id) {
        data = new HashMap<>(5);
        data.put("type", type);
        data.put("name", name);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Device(String[] properties, String[] values, int id) {
        data = new HashMap<>(properties.length);
        if (properties.length < values.length) {
            return;
        }
        int var1 = 0;
        for (String property : properties) {
            String value = var1 >= values.length ? "" : values[var1++];
            data.put(property, value);
        }
        data.putIfAbsent("type", "?");
        this.id = id;
    }

    public String getType() {
        return data.get("type");
    }

    public boolean isEmpty() {
        return data == null;
    }

}
