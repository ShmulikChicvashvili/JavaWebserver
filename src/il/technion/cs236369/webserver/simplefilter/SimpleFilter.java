package il.technion.cs236369.webserver.simplefilter;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * A filter is an object that performs filtering tasks on the response from a resource. 
 * Filters perform filtering in the doFilter method
 * 
 * Filters are configured in the deployment descriptor of a server
 *
 */
public interface SimpleFilter {
	/**
	 * the doFilter method of the Filter is called by the server each time a request/response pair is matched to the filter pattern.
	 * The FilterChain passed in to this method allows the Filter to pass on the request and response to the next entity in the chain.
	 * @param request
	 * @param response
	 * @param chain
	 */
	void doFilter(HttpRequest request, HttpResponse response, FilterChain chain);
}
