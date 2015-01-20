
package il.technion.cs236369.webserver.simplefilter;


import java.util.Set;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;




public class SimpleFilterWrapper
{
	
	public SimpleFilterWrapper(SimpleFilter filter, Set<String> urlPatterns)
	{
		super();
		this.filter = filter;
		this.urlPatterns = urlPatterns;
	}
	
	
	public void doFilter(
		HttpRequest request,
		HttpResponse response,
		FilterChain chain)
	{
		filter.doFilter(request, response, chain);
	}
	
	
	public boolean isMatching(String url)
	{
		if (url.contains("."))
		{
			final String extension = url.substring(url.lastIndexOf("."));
			if (urlPatterns.contains("*" + extension)) { return true; }
		}
		return false;
	}
	
	
	
	private final SimpleFilter filter;
	
	private final Set<String> urlPatterns;
	
}
