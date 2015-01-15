package il.technion.cs236369.webserver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpRequest;
import org.apache.http.HttpServerConnection;

public class RequestObject {

	public RequestObject(HttpServerConnection conn, HttpRequest request,
			String baseDir) {
		super();
		this.conn = conn;
		this.request = request;
		URI uri = null;
		try {
			uri = new URI(request.getRequestLine().getUri());
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Request object with URI = " + uri.toString());

		path = Paths.get(baseDir, uri.getPath());
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
