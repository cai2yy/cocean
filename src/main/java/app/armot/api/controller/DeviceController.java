package app.armot.api.controller;


import annotations.Inject;
import annotations.Named;
import annotations.Singleton;
import app.armot.api.bean.Device;
import app.armot.api.service.ComponentService;
import app.armot.api.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.armot.server.Controller;
import app.armot.server.HttpContext;
import app.armot.server.HttpRequest;
import app.armot.server.Router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 查看和控制智能设备
 * @date 2020/2/21 18:20
 */

@Singleton
public class DeviceController implements Controller {

    @Inject
    @Named("DeviceService")
    DeviceService deviceService;

    @Inject
    @Named("ComponentService")
    ComponentService componentService;

    private final static Logger LOG = LoggerFactory.getLogger(DeviceController.class);

    public DeviceController() {
        LOG.info("IotController成功创建");
    }

    @Override
    public Router route() {
        return new Router()
                .handler("/", "GET", this::showDevice)
                .handler("/int", "GET", this::getDevice)
                .handler("/new", "GET", this::getCreatePage)
                .handler("/new", "POST", this::createDevice);
    }

    public void getCreatePage(HttpContext ctx, HttpRequest req) {
        ctx.render("newDevice.ftl");
    }

    public void createDevice(HttpContext ctx, HttpRequest req) {
        String type = req.mixedParam("deviceType");
        String name = req.mixedParam("deviceName");
        Device device = deviceService.createDevice(type, name);
        if (type == null) {
            ctx.abort(500, "错误！请输入设备类型");
            return;
        }
        ctx.redirect("/device/" + device.getId());
    }

    public void getDevice(HttpContext ctx, HttpRequest req) {
        String path = req.path();
        int deviceId = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        Device device = deviceService.getDevice(deviceId);
        if (device == null) {
            ctx.abort(404, "错误！没有找到该设备");
            return;
        }

        var params = new HashMap<String, Object>();
        params.put("req", req);
        params.put("abc", device.getType());
        ctx.render("show.ftl", params);

    }

    public void showDevice(HttpContext ctx, HttpRequest req) {
        System.out.println("展示所有设备???");

        var params = new HashMap<String, Object>();
        params.put("req", req);

        // 此处的params可以是多重Map，例如按以下写法，前端可由{abc.a}获取到"TestSuccess"
        params.put("abc", "TestSuccess");

        ctx.render("show.ftl", params);
    }

    public void RPC(HttpContext ctx, HttpRequest req) {
        System.out.println(req.allParams());
        System.out.println("RPC方法");
    }

    public void RPC(long methodId, Object ...args) throws InvocationTargetException, IllegalAccessException {
        int componentId = (int) (methodId >> 32);
        String componentName = componentService.getComponentName(componentId);
        int methodNum = (int) (methodId & 0x000000ffffffL);

        Method method = componentService.getMethod(componentName, methodNum);
        Class<?> deviceType = componentService.getComponentType(componentName);
        //todo 解决这里
        //Object componentInstance = Injector.getInjector().getInstance(deviceType);

        //method.invoke(componentInstance, args);
    }

}
