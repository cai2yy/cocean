package webapp;

import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Cai2yy
 * @date 2020/3/24 14:18
 */

public class NettySuccessor extends ChannelInboundHandlerAdapter {

    public void print() {
        System.out.println("我继承了netty里的一个类");
    }


}
