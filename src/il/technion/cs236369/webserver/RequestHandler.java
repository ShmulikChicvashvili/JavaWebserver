package il.technion.cs236369.webserver;

import il.technion.cs236369.webserver.simplefilter.FilterChain;
import il.technion.cs236369.webserver.simplefilter.FilterChainImpl;
import il.technion.cs236369.webserver.simplefilter.SimpleFilterWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseContent;

public class RequestHandler extends Thread {

	public RequestHandler(LinkedBlockingQueue<RequestObject> requestsQueue,
			Map<String, String> extension2contentType,
			List<SimpleFilterWrapper> filters, String baseDir) {
		super();
		this.requestsQueue = requestsQueue;
		this.extension2contentType = extension2contentType;
		this.filterChain = new FilterChainImpl(filters);
		this.baseDir = baseDir;
	}

	@Override
	public void run() {
		proc = HttpProcessorBuilder.create().add(new ResponseContent())
				.add(new HttpResponseInterceptor() {

					@Override
					public void process(HttpResponse response,
							HttpContext context) throws HttpException,
							IOException {
						if (response.containsHeader(HttpHeaders.CONNECTION)) {
							response.removeHeaders(HttpHeaders.CONNECTION);
						}
						response.addHeader(HttpHeaders.CONNECTION, "close");
					}
				}).build();
		WebServerLog.log(this, "Request handler has started running");

		while (true) {
			try {
				RequestObject reqObj = requestsQueue.take();
				WebServerLog.log(this, "Request handler got a new request");

				handleRequest(reqObj);

				reqObj.getConn().shutdown();
			} catch (InterruptedException e) {
				WebServerLog.log(this, "Request handler was interrupted");
				e.printStackTrace();
			} catch (IOException e) {
				WebServerLog.log(this,
						"Request handler failed to close connection");
				e.printStackTrace();
			}
		}

	}

	private void handleRequest(RequestObject reqObj) {
		HttpServerConnection conn = reqObj.getConn();
		HttpRequest req = reqObj.getRequest();
		WebServerLog.log(this,
				"Request handler got a request:\n" + req.toString());

		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, ReasonPhrases.OK);

		WebServerLog.log(this,
				"Request handler checking file: " + reqObj.getPath().toString());
		File file = new File(reqObj.getPath().toString());
		if (!file.exists()) {
			WebServerLog.log(this, "File " + reqObj.getPath().toString()
					+ " Does not exist");
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
		} else {
			FileEntity entity = new FileEntity(file);
			response.setEntity(entity);
		}
		try {
			proc.process(response, new BasicHttpContext());
		} catch (HttpException | IOException e1) {
			WebServerLog.log(this,
					"Request handler failed to 'process' response");
			e1.printStackTrace();
		}

		WebServerLog.log(this, "Request handler is sending a response:\n"
				+ response.toString());

		try {
			conn.sendResponseEntity(response);
		} catch (HttpException | IOException e) {
			WebServerLog
					.log(this,
							"Request handler has encountered error while sending response");
			e.printStackTrace();
		}
	}

	private String baseDir;

	private LinkedBlockingQueue<RequestObject> requestsQueue;

	private Map<String, String> extension2contentType;

	private FilterChain filterChain;

	private HttpProcessor proc;
}
