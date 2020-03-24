package app.armot.server.internal;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

@Sharable
public class MessageCollector extends ChannelInboundHandlerAdapter {
	private final static Logger LOG = LoggerFactory.getLogger(MessageCollector.class);

	private ThreadPoolExecutor[] executors;
	private IRequestDispatcher dispatcher;
	private int requestsMaxInflight = 1000;

	public MessageCollector(int workerThreads, IRequestDispatcher dispatcher) {
		var factory = new ThreadFactory() {

			AtomicInteger seq = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("http-" + seq.getAndIncrement());
				return t;
			}

		};

		this.executors = new ThreadPoolExecutor[workerThreads];
		for (int i = 0; i < workerThreads; i++) {
			var queue = new ArrayBlockingQueue<Runnable>(requestsMaxInflight);
			this.executors[i] = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, queue, factory,
					new CallerRunsPolicy());
		}
		this.dispatcher = dispatcher;
	}

	public void closeGracefully() {
		for (ThreadPoolExecutor executor : executors) {
			executor.shutdown();
		}
		for (ThreadPoolExecutor executor : executors) {
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException ignored) {
			}
			executor.shutdownNow();
		}
	}

	/**
	 * 有新客户端连接连入
	 * @param ctx 绑定的handler上下文
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("connection comes {}", ctx.channel().remoteAddress());
	}

	/**
	 * 有客户端断开连接
	 * @param ctx 绑定的handler上下文
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("connection leaves {}", ctx.channel().remoteAddress());
		ctx.close();
	}

	/**
	 * 读取客户端传入信息
	 * //todo 核心开发部分
	 * @param ctx 绑定的handler上下文
	 * @param msg 客户端传入的信息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			var req = (FullHttpRequest) msg;
			var crc32 = new CRC32();
			crc32.update(ctx.hashCode());
			int idx = (int) (crc32.getValue() % executors.length);
			this.executors[idx].execute(() -> {
				dispatcher.dispatch(ctx, req);
			});
		}
	}

	/**
	 * 错误拦截器
	 * @param ctx 绑定的handler上下文
	 * @param cause 报错原因
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
