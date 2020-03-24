package app.armot.api.service;

import annotations.Inject;
import annotations.Named;
import annotations.Singleton;
import app.armot.api.bean.Device;
import app.armot.core.ArmOT;
import core.Injector;

import java.util.Map;

/**
 * @author Cai2yy
 * @date 2020/2/21 23:34
 */
@Singleton
@Named("DeviceService")
public class DeviceServiceImpl implements DeviceService {

    @Inject
    ArmOT armOT;

    //todo 这个要懒加载
    Map<Integer, Device> devices;

    public DeviceServiceImpl() {

    }

    @Override
    public String getDeviceType(int deviceId) {
        //todo 读取配置yml文件，轻度数据持久化
        return devices.get(deviceId).getType();
    }

    @Override
    public Device getDevice(int deviceId) {
        return devices.get(deviceId);
    }

    @Override
    public Device createDevice(String deviceType) {
        return createDevice(deviceType, "");
    }

    @Override
    public Device createDevice(String deviceType, String deviceName) {
        var num = devices.size();
        while (devices.containsKey(num)) {
            num += 1;
        }
        Device newDevice = null;
        if (deviceName.length() == 0) {
            newDevice = new Device(deviceType, num);
        }
        else {
            newDevice = new Device(deviceType, deviceName, num);
        }
        devices.put(num, newDevice);
        return newDevice;
    }

    @Override
    public Device createDevice(String[] properties, String[] values) {
        var num = devices.size();
        while (devices.containsKey(num)) {
            num += 1;
        }
        Device newDevice = new Device(properties, values, num);
        devices.put(num, newDevice);
        return newDevice;
    }

}
