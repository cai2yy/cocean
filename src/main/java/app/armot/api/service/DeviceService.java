package app.armot.api.service;


import app.armot.api.bean.Device;

/**
 * @author Cai2yy
 * @date 2020/2/21 23:34
 */

public interface DeviceService {

    public String getDeviceType(int entityId);

    public Device getDevice(int DeviceId);

    public Device createDevice(String deviceType);

    public Device createDevice(String deviceType, String deviceName);

    public Device createDevice(String[] properties, String[] values);

}
