package il.technion.cs236369.webserver.simplefilter;

import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class FilterChainImpl implements FilterChain {

	public FilterChainImpl(List<SimpleFilterWrapper> filters) {
		this.filters = filters;
		index = 0;
	}

	@Override
	public void doFilter(HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub

	}

	private List<SimpleFilterWrapper> filters;
	private int index;
}
