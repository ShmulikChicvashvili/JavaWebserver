package il.technion.cs236369.webserver;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import javax.net.ServerSocketFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class WebServerTestModule extends AbstractModule {
	
	private final Properties properties;
	
	private final ServerSocketFactory mockedServerSocketFactory = mock(ServerSocketFactory.class);
	private final ServerSocket mockedServerSocket = mock(ServerSocket.class);
	
	private Properties getDefaultProperties() {
		Properties defaultProps = new Properties();
		
		defaultProps.setProperty("httpserver.net.port", "8080");
		defaultProps.setProperty("httpserver.app.welcomeFile", "index.html");
		defaultProps.setProperty("httpserver.app.baseDir", "c:\\hw4\\serverBase\\");
		defaultProps.setProperty("httpserver.threads.numSocketReaders", "3");
		defaultProps.setProperty("httpserver.threads.numRequestHandlers", "7");
		defaultProps.setProperty("httpserver.queues.sizeSocketQueue", "100");
		defaultProps.setProperty("httpserver.queues.sizeRequestQueue", "200");
		defaultProps.setProperty("httpserver.session.timeout", "300000");

		return defaultProps;
	}
	
	public WebServerTestModule() {
		this(new Properties());
	}
	
	public WebServerTestModule(Properties properties) {
		this.properties = getDefaultProperties();
		this.properties.putAll(properties);
	}
	
	public WebServerTestModule setProperty(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}
	
	
	
	public ServerSocketFactory getMockedServerSocketFactory() {
		return mockedServerSocketFactory;
	}
	
	public ServerSocket getMockedServerSocket() {
		return mockedServerSocket;
	}
	
	@Override
	protected void configure() {
		Names.bindProperties(binder(), properties);
		
		try {
			when(mockedServerSocketFactory.createServerSocket(anyInt()))
				.thenReturn(mockedServerSocket);
		} catch (IOException e) {
			throw new AssertionError();
		}
		
		bind(ServerSocketFactory.class).toInstance(mockedServerSocketFactory);
		
		bind(WebServer.class).in(Scopes.SINGLETON);
	}
}
