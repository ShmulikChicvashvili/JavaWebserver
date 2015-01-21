/**
 *
 */

package il.technion.cs236369.webserver;

import il.technion.cs236369.webserver.simplefilter.GZipSimpleFilter;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

/**
 * @author Shmulik
 *
 */
public class HttpClientForTesting {
	public static void main(String[] args) throws Exception {
		final HttpProcessor httpproc = HttpProcessorBuilder
				.create()
				.add(new RequestContent())
				.add(new RequestTargetHost())
				.add(new HttpRequestInterceptor() {

					@Override
					public void process(HttpRequest arg0, HttpContext arg1)
							throws HttpException, IOException {
						arg0.addHeader("Accept-Encoding", "gzip");

					}
				}).add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new RequestExpectContinue(true)).build();

		final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		final HttpCoreContext coreContext = HttpCoreContext.create();
		final HttpHost host = new HttpHost("localhost", 8080);
		coreContext.setTargetHost(host);

		final DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
				8 * 1024);
		final ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

		try {

			final String[] targets = { "http://localhost:8080/index.html",
					"http://localhost:8080", "/bla.txt", "/bla.html",
					"http://localhost:8080/bla.html", "none_existing.html",
					"/dir", "dir", "/dir/a" };

			for (final String target : targets) {
				if (!conn.isOpen()) {
					final Socket socket = new Socket(host.getHostName(),
							host.getPort());
					conn.bind(socket);
				}

				final BasicHttpRequest request = new BasicHttpRequest("GET",
						target);
				System.out.println(">> Request URI: "
						+ request.getRequestLine().getUri());

				httpexecutor.preProcess(request, httpproc, coreContext);
				for (final Header h : request.getAllHeaders()) {
					System.out.println(h.toString());
				}

				final HttpResponse response = httpexecutor.execute(request,
						conn, coreContext);
				System.out.println("<< Before processing response");
				for (final Header h : response.getAllHeaders()) {
					System.out.println(h.toString());
				}
				httpexecutor.postProcess(response, httpproc, coreContext);

				System.out.println("<< Response: " + response.getStatusLine());
				for (final Header h : response.getAllHeaders()) {
					System.out.println(h.toString());
				}
				System.out.println("=====================");
				if (response.containsHeader("Content-Encoding")
						&& response.getFirstHeader("Content-Encoding")
								.getValue().contains("gzip")) {
					final String out = GZipSimpleFilter.decompress(EntityUtils
							.toByteArray(response.getEntity()));
					System.out.println(out);

				} else {
					System.out.println(EntityUtils.toString(response
							.getEntity()));
				}
				System.out.println("=====================");
				System.out.println("");

				if (!connStrategy.keepAlive(response, coreContext)) {
					conn.close();
				} else {
					System.out.println("Connection kept alive...");
				}
			}
		} finally {
			conn.close();
		}
	}
}
