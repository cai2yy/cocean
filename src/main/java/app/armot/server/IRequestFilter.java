package app.armot.server;

@FunctionalInterface
public interface IRequestFilter {

	/**
	 * @param ctx
	 * @param req
	 * @param beforeOrAfter true for before, false for after
	 * @return whether to continue filter pipeline
	 */
	public boolean filter(HttpContext ctx, HttpRequest req, boolean beforeOrAfter);

}
