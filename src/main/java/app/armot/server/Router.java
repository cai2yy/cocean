package app.armot.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;
import java.util.regex.Pattern;

public class Router {

	private HandlerFunction wildcardHandler;

	private Map<String, HandlerFunction> subHandlers = new HashMap<>();
	private Map<String, Map<String, HandlerFunction>> subMethodHandlers = new HashMap<>();

	private Map<String, Router> subRouters = new HashMap<>();

	private List<IRequestFilter> filters = new ArrayList<>();

	private final static List<String> METHODS = Arrays
			.asList("get", "post", "head", "put", "delete", "trace", "options", "patch", "connect");

	public Router() {
		this(null);
	}

	public Router(HandlerFunction wildcardHandler) {
		this.wildcardHandler = wildcardHandler;
	}

	public Router handler(String path, HandlerFunction handler) {
		path = HttpUtils.normalize(path);
		if (path.indexOf('/') != path.lastIndexOf('/')) {
			throw new IllegalArgumentException("path at most one slash allowed");
		}
		this.subHandlers.put(path, handler);
		return this;
	}

	public Router handler(String path, String method, HandlerFunction handler) {
		path = HttpUtils.normalize(path);
		method = method.toLowerCase();
		if (path.indexOf('/') != path.lastIndexOf('/')) {
			throw new IllegalArgumentException("path at most one slash allowed");
		}
		if (!METHODS.contains(method)) {
			throw new IllegalArgumentException("illegal http method name");
		}
		var handlers = subMethodHandlers.computeIfAbsent(path, k -> new HashMap<>());
		handlers.put(method, handler);
		return this;
	}

	public Router child(String path, Router router) {
		path = HttpUtils.normalize(path);
		if (path.equals("/")) {
			throw new IllegalArgumentException("child path should not be /");
		}
		if (path.indexOf('/') != path.lastIndexOf('/')) {
			throw new IllegalArgumentException("path at most one slash allowed");
		}
		this.subRouters.put(path, router);
		return this;
	}

	// 执行传入Controller类的route()函数，并得到子路由（Router类），绑定到根路由上
	public Router child(String path, Controller routeable) {
		return child(path, routeable.route());
	}

	public Router resource(String path, String resourceRoot) {
		Router router = new Router(new StaticRequestHandlerFunction(resourceRoot));
		return child(path, router);
	}

	public Router resource(String path, String resourceRoot, boolean classpath) {
		Router router = new Router(new StaticRequestHandlerFunction(resourceRoot, classpath));
		return child(path, router);
	}

	public Router resource(String path, String resourceRoot, boolean classpath, int cacheAge) {
		Router router = new Router(new StaticRequestHandlerFunction(resourceRoot, classpath, cacheAge));
		return child(path, router);
	}

	public Router filter(IRequestFilter... filters) {
		for (var filter : filters) {
			this.filters.add(filter);
		}
		return this;
	}

	public void handle(HttpContext ctx, HttpRequest req) {
		for (var filter : filters) {
			req.filter(filter);
		}
		var prefix = req.peekUriPrefix();
		var method = req.method().toLowerCase();
		var router = subRouters.get(prefix);
		if (router != null) {
			req.popUriPrefix();
			router.handle(ctx, req);
			return;
		}

		//todo 此处有改动
		if (prefix.equals(req.relativeUri())) {
			Map<String, HandlerFunction> handlers;
			if (!req.relativeUri().equals("/") &&
					Pattern.compile("^[-\\+]?[\\d]*$").matcher(req.relativeUri().substring(1)).matches()) {
				handlers = subMethodHandlers.get("/int");
			}
			else {
				handlers = subMethodHandlers.get(prefix);
			}
			if (handlers != null) {
				HandlerFunction handler = handlers.get(method);

				if (handler == null) {
					handler = subHandlers.get(prefix);
				}
				if (handler != null) {
					handleImpl(handler, ctx, req);
					return;
				}
			}
		}

		if (this.wildcardHandler != null) {
			handleImpl(wildcardHandler, ctx, req);
			return;
		}

		throw new AbortException(HttpResponseStatus.NOT_FOUND);
	}

	private void handleImpl(HandlerFunction handler, HttpContext ctx, HttpRequest req) {
		for (var filter : req.filters()) {
			if (!filter.filter(ctx, req, true)) {
				return;
			}
		}
		handler.handle(ctx, req);

		for (var filter : req.filters()) {
			if (!filter.filter(ctx, req, false)) {
				return;
			}
		}
	}

}
