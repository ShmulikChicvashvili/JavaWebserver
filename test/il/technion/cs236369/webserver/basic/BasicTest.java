package il.technion.cs236369.webserver.basic;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import il.technion.cs236369.webserver.WebServer;
import il.technion.cs236369.webserver.WebServerTestModule;
import il.technion.cs236369.webserver.test.ResponseExpectingSocket;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Client send response to the server, the server should call server socket
 * factory and close the socket.
 */
public class BasicTest {

	private final static String requestedURL = "http://localhost:8080/blabla/index.html";

	private static WebServerTestModule module;
	private static ResponseExpectingSocket clientToServerSocket1;

	@BeforeClass
	public static void sendOneRequestAndRecvACachableResponse()
			throws Exception {

		module = new WebServerTestModule();
		HttpRequest req;
		req = new BasicHttpRequest("GET", requestedURL);
		URI uri = new URI(requestedURL);
		req.addHeader("Host", uri.getHost());

		clientToServerSocket1 = spy(new ResponseExpectingSocket(req));

		when(module.getMockedServerSocket().accept())
        .thenReturn(clientToServerSocket1)
        .thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Thread.sleep(10000);
                return null;
            }
        });

		Injector injector = null;
		try {
			injector = Guice.createInjector(module);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}

		final WebServer server = injector.getInstance(WebServer.class);

		server.bind();
		new Thread(new Runnable() {
			@Override
			public void run() {
				server.start();
			}
		}).start();

		Thread.sleep(4000);
	}

	@Test
	public void test1() throws IOException {
		// check the server listens on the correct port
		try {
			verify(module.getMockedServerSocketFactory(), times(1))
					.createServerSocket(8080);
		} catch (MockitoAssertionError e) {
			System.err
					.println("Please call one time only to createServerSocket, by ServerSocketFactory.");
			throw e;
		}
		try {
			// check all sockets were closed
			verify(clientToServerSocket1, times(1)).close();
		} catch (MockitoAssertionError e) {
			System.err
					.println("Please close the socket to the client when done responding.");
			throw e;
		}
	}
}
