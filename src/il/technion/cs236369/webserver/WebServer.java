package il.technion.cs236369.webserver;

import il.technion.cs236369.webserver.simplefilter.SimpleFilterWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ServerSocketFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpDateGenerator;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseContent;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class WebServer extends AbstractWebServer {

	public static void main(String[] args) throws Exception {
		final Properties p = new Properties();
		p.load(new FileInputStream("config"));
		final Injector inj = Guice.createInjector(new WebServerModule(p));
		final IWebServer server = inj.getInstance(WebServer.class);
		server.bind();
		server.start();

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

		XMLParser parser = null;
		try {
			parser = new XMLParser();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			WebServerLog.log(this, "XML Parser has thrown an exception");
			e.printStackTrace();
		}
		extension2ContentType = parser.getMimeTypes();

		welcomeFile = parser.getWelcomeFile();

		filters = parser.getFilterWrappers();

		socketsQueue = new LinkedBlockingQueue<>(sizeSocketQueue);
		requestsQueue = new LinkedBlockingQueue<>(sizeRequestQueue);

		socketReaders = new LinkedList<SocketReader>();
		for (int i = 0; i < numSocketReaders; i++) {
			final SocketReader reader = new SocketReader(socketsQueue,
					requestsQueue, baseDir, welcomeFile);
			socketReaders.add(reader);
		}
		requestHandlers = new LinkedList<RequestHandler>();
		for (int i = 0; i < numRequestHandlers; i++) {
			final RequestHandler handler = new RequestHandler(requestsQueue,
					extension2ContentType, filters, baseDir);
			requestHandlers.add(handler);
		}

	}

	@Override
	public void bind() throws IOException {
		WebServerLog.log(this, "Server is binding");
		serverSocket = srvSockFactory.createServerSocket(port);

		proc = HttpProcessorBuilder.create().add(new ResponseContent(true))
				.add(new HttpResponseInterceptor() {

					@Override
					public void process(HttpResponse response,
							HttpContext context) throws HttpException,
							IOException {
						if (response.containsHeader(HttpHeaders.CONNECTION)) {
							response.removeHeaders(HttpHeaders.CONNECTION);
						}
						response.addHeader(HttpHeaders.CONNECTION, "close");

						if (response.containsHeader(HttpHeaders.DATE)) {
							response.removeHeaders(HttpHeaders.DATE);
						}
						response.addHeader(HttpHeaders.DATE,
								new HttpDateGenerator().getCurrentDate());
					}
				}).build();
	}

	@Override
	public void start() {
		// TODO: Check it with two filters
		WebServerLog.log(this, "Server has started");
		for (final SocketReader reader : socketReaders) {
			reader.start();
		}
		for (final RequestHandler handler : requestHandlers) {
			handler.start();
		}

		try {
			while (true) {
				final Socket clientSock = serverSocket.accept();
				if (socketsQueue.offer(clientSock)) {
					continue;
				}

				DefaultBHttpServerConnection conn = DefaultBHttpServerConnectionFactory.INSTANCE
						.createConnection(clientSock);
				HttpResponse response = generate503ErrorResponse();
				try {
					proc.process(response, new BasicHttpContext());
					assert response.containsHeader(HttpHeaders.DATE)
							&& response
									.containsHeader(HttpHeaders.CONTENT_LENGTH)
							&& response
									.containsHeader(HttpHeaders.CONTENT_TYPE)
							&& response.containsHeader(HttpHeaders.CONNECTION);

					conn.sendResponseHeader(response);
					conn.sendResponseEntity(response);
					conn.shutdown();
				} catch (HttpException e) {
					System.err
							.println("Error with sending 503 response to client");
					e.printStackTrace();
				} catch (IOException e) {
					System.err
							.println("Error with sending 503 response to client");
					e.printStackTrace();
				}
			}
		} catch (final IOException e) {
			System.err.println("Server has encountered an error. exiting.");
			e.printStackTrace();
		}
	}

	private HttpResponse generate503ErrorResponse() {
		BasicHttpResponse response = new BasicHttpResponse(
				HttpVersion.HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE,
				ReasonPhrases.SERVICE_UNAVAILABE);

		String fileName = "service_unavailable.html";
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		File file = new File(fileName);
		FileEntity entity = new FileEntity(file, ContentType.TEXT_HTML);

		response.setEntity(entity);
		assert (extension.equals("html"));
		response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html");

		return response;
	}

	private final Map<String, String> extension2ContentType;

	private final List<SimpleFilterWrapper> filters;

	private String welcomeFile;

	private final List<SocketReader> socketReaders;

	private final LinkedBlockingQueue<Socket> socketsQueue;

	private final List<RequestHandler> requestHandlers;

	private final LinkedBlockingQueue<RequestObject> requestsQueue;

	private ServerSocket serverSocket;

	private HttpProcessor proc;
}
