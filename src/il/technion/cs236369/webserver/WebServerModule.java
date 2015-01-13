package il.technion.cs236369.webserver;

import java.util.Properties;

import javax.net.ServerSocketFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class WebServerModule extends AbstractModule {

	private final Properties properties;

	private Properties getDefaultProperties() {
		Properties defaultProps = new Properties();

		defaultProps.setProperty("httpserver.net.port", "8080");
		defaultProps.setProperty("httpserver.app.baseDir",
				"c:\\hw4\\serverBase");
		defaultProps.setProperty("httpserver.threads.numSocketReaders", "3");
		defaultProps.setProperty("httpserver.threads.numRequestHandlers", "7");
		defaultProps.setProperty("httpserver.queues.sizeSocketQueue", "100");
		defaultProps.setProperty("httpserver.queues.sizeRequestQueue", "200");
		defaultProps.setProperty("httpserver.session.timeout", "300000");
		return defaultProps;
	}

	public WebServerModule() {
		this(new Properties());
	}

	public WebServerModule(Properties properties) {
		this.properties = getDefaultProperties();
		this.properties.putAll(properties);
	}

	public WebServerModule setProperty(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), properties);

		bind(ServerSocketFactory.class).toInstance(
				ServerSocketFactory.getDefault());

		bind(WebServer.class).in(Scopes.SINGLETON);
	}

}
