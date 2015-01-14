package il.technion.cs236369.webserver;

import java.net.Socket;

import org.apache.http.HttpRequest;
import org.apache.http.HttpServerConnection;

public class RequestObject {

	public RequestObject(HttpServerConnection conn, HttpRequest request,
			String baseDir) {
		super();
		this.conn = conn;
		this.request = request;
		this.url = request.getRequestLine().getUri();
	}

	public HttpServerConnection getConn() {
		return conn;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public String getUrl() {
		return url;
	}

	private HttpServerConnection conn;

	private HttpRequest request;

	private String url;
}
