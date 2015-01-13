package il.technion.cs236369.webserver.simplefilter;

import java.util.Set;

public class SimpleFilterWrapper {

	private SimpleFilter filter;
	private Set<String> urls;

	public SimpleFilterWrapper(SimpleFilter filter, Set<String> urls) {
		super();
		this.filter = filter;
		this.urls = urls;
	}

}
