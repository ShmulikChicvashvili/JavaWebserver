package il.technion.cs236369.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

import il.technion.cs236369.webserver.WebServerLog;

public class SocketReader extends Thread {

	public SocketReader(LinkedBlockingQueue<Socket> socketsQueue,
			LinkedBlockingQueue<RequestObject> requestsQueue, String baseDir) {
		super();
		this.socketsQueue = socketsQueue;
		this.requestsQueue = requestsQueue;
		this.baseDir = baseDir;
	}

	@Override
	public void run() {
		WebServerLog.log(this, "Socket reader has started running");
		HttpServerConnection conn = null;
		while (true) {
			try {
				Socket clientSock = socketsQueue.take();
				WebServerLog.log(this, "Socket reader got a new socket");

				conn = DefaultBHttpServerConnectionFactory.INSTANCE
						.createConnection(clientSock);
				HttpRequest request = conn.receiveRequestHeader();

				RequestObject reqObj = new RequestObject(conn, request, baseDir);
				requestsQueue.put(reqObj);
				WebServerLog.log(this, "Socket reader registered a new request");
			} catch (InterruptedException e) {
				WebServerLog.log(this, "Socket reader was interrupted");
				e.printStackTrace();
			} catch (IOException e) {
				WebServerLog.log(this,
						"Socket reader failen to create connection");
				e.printStackTrace();
			} catch (HttpException e) {
				WebServerLog.log(this, "Socket reader failed to read request");
				e.printStackTrace();
				try {
					conn.shutdown();
				} catch (final IOException ignore) {
					WebServerLog.log(this,
							"HttoServerConnection shutdown failed");
				}
			}

		}
	}

	private LinkedBlockingQueue<Socket> socketsQueue;

	private LinkedBlockingQueue<RequestObject> requestsQueue;

	private String baseDir;
}
