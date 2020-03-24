package app.armot.server.internal;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
	private final static Logger LOG = LoggerFactory.getLogger(HttpServer.class);

	private String ip;
	private int port;
	private int ioThreads;
	private int workerThreads;
	private IRequestDispatcher dispatcher;

	public HttpServer(String ip, int port, int ioThreads, int workerThreads, IRequestDispatcher dispatcher) {
		this.ip = ip;
		this.port = port;
		this.ioThreads = ioThreads;
		this.workerThreads = workerThreads;
		this.dispatcher = dispatcher;
	}

	private ServerBootstrap bootstrap;
	private EventLoopGroup group;
	private Channel serverChannel;
	private MessageCollector collector;

	public void start() {
		bootstrap = new ServerBootstrap();
		group = new NioEventLoopGroup(ioThreads);
		bootstrap.group(group);
		// dispatcher -> collector
		collector = new MessageCollector(workerThreads, dispatcher);
		bootstrap.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				var pipe = ch.pipeline();
				// 连接超时控制器
				pipe.addLast(new ReadTimeoutHandler(10));
				// Http的消息解码器
				pipe.addLast(new HttpServerCodec());
				// Http消息聚合器，解析POST，PUT请求中的参数
				pipe.addLast(new HttpObjectAggregator(1 << 30)); // max_size = 1g
				// 对异步读写大文件的支持
				pipe.addLast(new ChunkedWriteHandler());
				// 自定义数据处理器（读）
				pipe.addLast(collector);
			}
		});
		bootstrap.option(ChannelOption.SO_BACKLOG, 100).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
		serverChannel = bootstrap.bind(this.ip, this.port).channel();
		LOG.info("http服务启动成功 @ {}:{}", ip, port);
	}

	/**
	 * 配合netty的优雅停机函数
	 */
	public void stop() {
		// 先关闭服务端套件字
		serverChannel.close();
		// 再斩断消息来源，停止io线程池
		group.shutdownGracefully();
		// 停止业务线程池
		collector.closeGracefully();
	}

}
