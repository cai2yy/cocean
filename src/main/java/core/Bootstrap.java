package core;

import annotations.Inject;
import io.netty.channel.ChannelInboundHandlerAdapter;
import structure.ApplicationConfig;

import java.lang.module.Configuration;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * @author Cai2yy
 * @date 2020/3/22 12:44
 */

public class Bootstrap {

    public static void main(String[] args) {
        ApplicationConfig applicationConfig = initConfiguration();
        Container container = initContainer(applicationConfig);
        ChannelInboundHandlerAdapter x = null;

    }

    public static ApplicationConfig initConfiguration() {
        ApplicationConfig config = new ApplicationConfig();
        // todo 读配置文件
        return config;
    }

    public static Container initContainer(ApplicationConfig config) {
        Container container = new Container(config);
        return container;
    }

}
