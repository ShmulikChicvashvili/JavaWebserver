package il.technion.cs236369.webserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpRequest;
import org.apache.http.HttpServerConnection;

public class RequestObject {

	public RequestObject(HttpServerConnection conn, HttpRequest request,
			String baseDir, String welcomeFile) throws URISyntaxException {
		super();
		this.conn = conn;
		this.request = request;
		URI uri = null;
		uri = new URI(request.getRequestLine().getUri());
		System.out.println("Request object with URI = " + uri.toString());

		path = Paths.get(baseDir, uri.getPath());
		if (path.equals(Paths.get(baseDir))) {
			path = Paths.get(baseDir, welcomeFile);
		}
		System.out.println("Request object created path = " + path.toString());
	}

	public HttpServerConnection getConn() {
		return conn;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public Path getPath() {
		return path;
	}

	private HttpServerConnection conn;

	private HttpRequest request;

	private Path path;
}
