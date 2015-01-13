package il.technion.cs236369.webserver.simplefilter;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
/**
 * A FilterChain is an object provided by the server to the developer giving a view into the invocation chain of a filtered request for a resource.
 * Filters use the FilterChain to invoke the next filter in the chain.
 *
 */
public interface FilterChain {
	/**
	 * Causes the next filter in the chain to be invoked.
	 * @param request
	 * @param response
	 */
	void doFilter(HttpRequest request, HttpResponse response);
}
