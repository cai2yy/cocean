package app.armot.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.armot.server.internal.IRequestDispatcher;

import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher implements IRequestDispatcher {
	private final static Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);

	private String contextRoot;
	private Router router;
	private Map<Integer, IExceptionHandler> exceptionHandlers = new HashMap<>();
	private IExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();

	private ITemplateEngine templateEngine = new ITemplateEngine() {
	};

	static class DefaultExceptionHandler implements IExceptionHandler {

		@Override
		public void handle(HttpContext ctx, AbortException e) {
			if (e.getStatus().code() == 500) {
				LOG.error("Internal Server Error", e);
			}
			ctx.error(e.getContent(), e.getStatus().code());
		}

	}

	public RequestDispatcher(Router router) {
		this("/", router);
	}

	public RequestDispatcher(String contextRoot, Router router) {
		this.contextRoot = HttpUtils.normalize(contextRoot);
		this.router = router;
	}

	public RequestDispatcher templateRoot(String templateRoot) {
		this.templateEngine = new FreemarkerEngine(templateRoot);
		return this;
	}

	public String root() {
		return contextRoot;
	}

	public RequestDispatcher exception(int code, IExceptionHandler handler) {
		this.exceptionHandlers.put(code, handler);
		return this;
	}

	public RequestDispatcher exception(IExceptionHandler handler) {
		this.defaultExceptionHandler = handler;
		return this;
	}

	public void dispatch(ChannelHandlerContext channelCtx, FullHttpRequest req) {
		var ctx = new HttpContext(channelCtx, contextRoot, templateEngine);
		try {
			this.handleImpl(ctx, new HttpRequest(req));
		} catch (AbortException e) {
			this.handleException(ctx, e);
		} catch (Exception e) {
			this.handleException(ctx, new AbortException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e));
		} finally {
			req.release();
		}
	}

	private void handleException(HttpContext ctx, AbortException e) {
		var handler = this.exceptionHandlers.getOrDefault(e.getStatus().code(), defaultExceptionHandler);
		try {
			handler.handle(ctx, e);
		} catch (Exception ex) {
			this.defaultExceptionHandler.handle(ctx, new AbortException(HttpResponseStatus.INTERNAL_SERVER_ERROR, ex));
		}
	}

	private void handleImpl(HttpContext ctx, HttpRequest req) throws Exception {
		if (req.decoderResult().isFailure()) {
			ctx.abort(400, "http protocol decode failed");
		}
		if (req.relativeUri().contains("./") || req.relativeUri().contains(".\\")) {
			ctx.abort(400, "insecure url not allowed");
		}
		if (!req.relativeUri().startsWith(contextRoot)) {
			throw new AbortException(HttpResponseStatus.NOT_FOUND);
		}
		req.popRootUri(contextRoot);
		router.handle(ctx, req);
	}

}
