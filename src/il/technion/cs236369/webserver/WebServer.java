package il.technion.cs236369.webserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.net.ServerSocketFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class WebServer extends AbstractWebServer {

	Map<String, String> extension2ContentType;

	protected String welcomeFile;

	public void setWelcomeFile(String welcomeFile) {
		this.welcomeFile = welcomeFile;
	}

	@Inject
	public WebServer(
			ServerSocketFactory srvSockFactory,
			@Named("httpserver.net.port") int port,
			@Named("httpserver.app.baseDir") String baseDir,
			@Named("httpserver.threads.numSocketReaders") int numSocketReaders,
			@Named("httpserver.threads.numRequestHandlers") int numRequestHandlers,
			@Named("httpserver.queues.sizeSocketQueue") int sizeSocketQueue,
			@Named("httpserver.queues.sizeRequestQueue") int sizeRequestQueue,
			@Named("httpserver.session.timeout") int sessionTimeout)
			throws ClassNotFoundException {
		super(srvSockFactory, port, baseDir, numSocketReaders,
				numRequestHandlers, sizeSocketQueue, sizeRequestQueue,
				sessionTimeout);
		// Add your code here
	}

	@Override
	public void bind() throws IOException {
		// Add your code here
	}

	@Override
	public void start() {
		// Add your code here
	}

	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		p.load(new FileInputStream("config"));
		Injector inj = Guice.createInjector(new WebServerModule(p));
		IWebServer server = inj.getInstance(WebServer.class);
		server.bind();
		server.start();

	}
}
