package il.technion.cs236369.webserver;

import javax.net.ServerSocketFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class AbstractWebServer implements IWebServer {
	// dependencies
	protected final ServerSocketFactory srvSockFactory;
	protected final int port;
	protected final int numSocketReaders;
	protected final int numRequestHandlers;
	protected final int sizeSocketQueue;
	protected final int sizeRequestQueue;
	protected final int sessionTimeout;
	protected String baseDir;

	/**
	 * Constructs the WebServer
	 * 
	 * @param srvSockFactory
	 *            The ServerSocketFactory to be used for creating a single
	 *            ServerSocket for listening for clients requests
	 * 
	 * @param port
	 *            The port number to bounded by the ServerSocket
	 * 
	 * @param baseDir
	 *            The base directory of the server in the file system (absolute
	 *            path)
	 * @param numSocketReaders
	 *            number of socket-reader threads
	 * @param numRequestHandlers
	 *            number of request-handler threads
	 * @param sizeSocketQueue
	 *            size of the socket queue (how many client sockets are saved)
	 * @param sizeRequestQueue
	 *            size of the socket queue (how many requests are saved)
	 * @param sessionTimeout
	 *            session timeout in milliseconds from last activity.
	 */

	@Inject
	public AbstractWebServer(ServerSocketFactory srvSockFactory,
			@Named("httpserver.net.port") int port,
			@Named("httpserver.app.baseDir") String baseDir,
			@Named("httpserver.threads.numSocketReaders") int numSocketReaders,
			@Named("httpserver.threads.numRequestHandlers") int numRequestHandlers,
			@Named("httpserver.queues.sizeSocketQueue") int sizeSocketQueue,
			@Named("httpserver.queues.sizeRequestQueue") int sizeRequestQueue,
			@Named("httpserver.session.timeout") int sessionTimeout
			)
			throws ClassNotFoundException {
		this.srvSockFactory = srvSockFactory;
		this.port = port;
		this.baseDir = baseDir;
		this.numSocketReaders = numSocketReaders;
		this.numRequestHandlers = numRequestHandlers;
		this.sizeSocketQueue = sizeSocketQueue;
		this.sizeRequestQueue = sizeRequestQueue;
		this.sessionTimeout = sessionTimeout;

	}
}
