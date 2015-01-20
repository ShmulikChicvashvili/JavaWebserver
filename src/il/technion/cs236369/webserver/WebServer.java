
package il.technion.cs236369.webserver;


import il.technion.cs236369.webserver.simplefilter.SimpleFilterWrapper;

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

import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;




public class WebServer extends AbstractWebServer
{
	
	public static void main(String[] args) throws Exception
	{
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
		throws ClassNotFoundException
	{
		super(
			srvSockFactory,
			port,
			baseDir,
			numSocketReaders,
			numRequestHandlers,
			sizeSocketQueue,
			sizeRequestQueue,
			sessionTimeout);
		
		XMLParser parser = null;
		try
		{
			parser = new XMLParser();
		} catch (ParserConfigurationException | SAXException | IOException e)
		{
			WebServerLog.log(this, "XML Parser has thrown an exception");
			e.printStackTrace();
		}
		System.out.println("HASDASODP");
		extension2ContentType = parser.getMimeTypes();
		System.out.println("HASDASODP");
		
		filters = parser.getFilterWrappers();
		System.out.println("HASDASODP");
		
		socketsQueue = new LinkedBlockingQueue<>(sizeSocketQueue);
		requestsQueue = new LinkedBlockingQueue<>(sizeRequestQueue);
		
		socketReaders = new LinkedList<SocketReader>();
		for (int i = 0; i < numSocketReaders; i++)
		{
			final SocketReader reader =
				new SocketReader(socketsQueue, requestsQueue, baseDir);
			socketReaders.add(reader);
		}
		requestHandlers = new LinkedList<RequestHandler>();
		for (int i = 0; i < numRequestHandlers; i++)
		{
			final RequestHandler handler =
				new RequestHandler(
					requestsQueue,
					extension2ContentType,
					filters,
					baseDir);
			requestHandlers.add(handler);
		}
		
	}
	
	
	@Override
	public void bind() throws IOException
	{
		WebServerLog.log(this, "Server is binding");
		serverSocket = srvSockFactory.createServerSocket(port);
	}


	// TODO use this somewhere somehow
	public void setWelcomeFile(String welcomeFile)
	{
		this.welcomeFile = welcomeFile;
	}
	
	
	@Override
	public void start()
	{
		// TODO: Check it with two filters
		WebServerLog.log(this, "Server has started");
		for (final SocketReader reader : socketReaders)
		{
			reader.start();
		}
		for (final RequestHandler handler : requestHandlers)
		{
			handler.start();
		}
		
		try
		{
			while (true)
			{
				final Socket clientSock = serverSocket.accept();
				if (socketsQueue.offer(clientSock))
				{
					continue;
				}
				// TODO send back an error 503
			}
		} catch (final IOException e)
		{
			System.err.println("Server has encountered an error. exiting.");
			e.printStackTrace();
		}
	}
	
	
	
	private final Map<String, String> extension2ContentType;
	
	private final List<SimpleFilterWrapper> filters;
	
	private String welcomeFile;
	
	private final List<SocketReader> socketReaders;
	
	private final LinkedBlockingQueue<Socket> socketsQueue;
	
	private final List<RequestHandler> requestHandlers;
	
	private final LinkedBlockingQueue<RequestObject> requestsQueue;
	
	private ServerSocket serverSocket;
}
