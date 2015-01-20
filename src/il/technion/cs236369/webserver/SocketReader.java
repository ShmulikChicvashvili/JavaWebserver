package il.technion.cs236369.webserver;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;

public class SocketReader extends Thread {

	public SocketReader(LinkedBlockingQueue<Socket> socketsQueue,
			LinkedBlockingQueue<RequestObject> requestsQueue, String baseDir,
			String welcomeFile) {
		super();
		this.socketsQueue = socketsQueue;
		this.requestsQueue = requestsQueue;
		this.baseDir = baseDir;
		this.welcomeFile = welcomeFile;
	}

	@Override
	public void run() {
		WebServerLog.log(this, "Socket reader has started running");
		while (true) {
			HttpServerConnection conn = null;
			boolean shutdown = true;
			try {
				final Socket clientSock = socketsQueue.take();
				WebServerLog.log(this, "Socket reader got a new socket");

				conn = DefaultBHttpServerConnectionFactory.INSTANCE
						.createConnection(clientSock);
				final HttpRequest request = conn.receiveRequestHeader();
				final RequestObject reqObj = new RequestObject(conn, request,
						baseDir, welcomeFile);
				requestsQueue.put(reqObj);
				shutdown = false;
				WebServerLog
						.log(this, "Socket reader registered a new request");
			} catch (final InterruptedException e) {
				WebServerLog.log(this, "Socket reader was interrupted");
				e.printStackTrace();
			} catch (final IOException e) {
				WebServerLog
						.log(this,
								"Socket reader failed to create connection or read the request");
				e.printStackTrace();
			} catch (final HttpException e) {
				WebServerLog.log(this, "Socket reader failed to read request");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				System.err
						.println("Socket Reader has encounter a problem with creating a URI from the requested URI");
				e.printStackTrace();
			} finally {
				if (shutdown) {
					System.err
							.println("Socket reader shutting down connection");
					try {
						conn.shutdown();
					} catch (final IOException ignore) {
						WebServerLog.log(this,
								"HttpServerConnection shutdown failed");
					}
				}
			}

		}
	}

	private final LinkedBlockingQueue<Socket> socketsQueue;

	private final LinkedBlockingQueue<RequestObject> requestsQueue;

	private final String baseDir;

	private final String welcomeFile;
}
