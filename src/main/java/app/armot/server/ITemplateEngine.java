package app.armot.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public interface ITemplateEngine {

	public default String render(String path, Map<String, Object> context) {
		throw new AbortException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "template root not provided");
	}

}
