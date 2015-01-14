package il.technion.cs236369.webserver.simplefilter;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class SimpleFilterWrapper {

	private SimpleFilter filter;
	private Set<Pattern> urlPatterns;

	public SimpleFilterWrapper(SimpleFilter filter, Set<String> urlPatterns) {
		super();
		this.filter = filter;
		for (String urlPattern : urlPatterns) {
			this.urlPatterns.add(Pattern.compile(urlPattern));
		}
	}

	public boolean isMatching(String url) {
		// FIXME add this method
		return false;
	}

	public void doFilter(HttpRequest request, HttpResponse response,
			FilterChain chain) {
		filter.doFilter(request, response, chain);
	}

}
