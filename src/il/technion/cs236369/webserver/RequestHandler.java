
package il.technion.cs236369.webserver;


import il.technion.cs236369.webserver.simplefilter.FilterChainImpl;
import il.technion.cs236369.webserver.simplefilter.SimpleFilterWrapper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpDateGenerator;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseContent;




public class RequestHandler extends Thread
{
	public RequestHandler(
		LinkedBlockingQueue<RequestObject> requestsQueue,
		Map<String, String> extension2contentType,
		List<SimpleFilterWrapper> filters,
		String baseDir)
	{
		super();
		this.requestsQueue = requestsQueue;
		this.extension2contentType = extension2contentType;
		filterChain = new FilterChainImpl(filters);
		this.baseDir = baseDir;
	}


	@Override
	public void run()
	{
		
		proc =
			HttpProcessorBuilder
				.create()
				.add(new ResponseContent(true))
				.add(new HttpResponseInterceptor()
				{

					@Override
					public void process(
						HttpResponse response,
						HttpContext context) throws HttpException, IOException
					{
						if (response.containsHeader(HttpHeaders.CONNECTION))
						{
							response.removeHeaders(HttpHeaders.CONNECTION);
						}
						response.addHeader(HttpHeaders.CONNECTION, "close");
						
						if (response.containsHeader(HttpHeaders.DATE))
						{
							response.removeHeaders(HttpHeaders.DATE);
						}
						response.addHeader(
							HttpHeaders.DATE,
							new HttpDateGenerator().getCurrentDate());
					}
				})
				.build();
		WebServerLog.log(this, "Request handler has started running");
		RequestObject reqObj = null;
		while (true)
		{
			try
			{
				reqObj = requestsQueue.take();
				
				// timer.schedule(new MyTimerTask(Thread.currentThread()),
				// // 5 * 60 * 1000);
				// 5 * 1000);

				WebServerLog.log(this, "Request handler got a new request");
				
				handleRequest(reqObj);
				
				reqObj.getConn().shutdown();
			} catch (final InterruptedException e)
			{
				WebServerLog.log(this, "Request handler was interrupted");
				try
				{
					reqObj.getConn().shutdown();
				} catch (final IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (final IOException e)
			{
				WebServerLog.log(
					this,
					"Request handler failed to close connection");
				e.printStackTrace();
			}
		}
		
	}
	
	
	private HttpEntity createNotFoundEntity()
	{
		HttpEntity entity = null;
		try
		{
			entity =
				new FileEntity(new File(getClass()
					.getResource("not_found.html")
					.toURI()));
		} catch (final URISyntaxException e)
		{
			WebServerLog.log(
				this,
				"Request handler failed to load not found file");
			e.printStackTrace();
		}
		return entity;
	}
	
	
	private boolean handleFileRequest(Path path, final HttpResponse response)
	{
		WebServerLog.log(
			this,
			"Request handler checking file: " + path.toString());
		
		final File file = new File(path.toString());
		HttpEntity entity = null;
		
		// bad request
		if (!path.startsWith(baseDir) || !file.exists() || !file.canRead())
		{
			WebServerLog.log(this, "File " + path.toString() + " is invalid");
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			response.setReasonPhrase(ReasonPhrases.NOT_FOUND);
			
			entity = createNotFoundEntity();
			response.setEntity(entity);
			response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html");
			return false;
		}
		
		assert response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		WebServerLog.log(this, "File "
			+ path.toString()
			+ " is locked and loaded!! THIS IS SPARTA!");
		if (file.isDirectory())
		{
			// directory request
			// FIXME the directory content should conform to some standard.
			String s = "";
			for (final String fileName : file.list())
			{
				s += fileName + "\n";
			}
			try
			{
				entity = new StringEntity(s);
			} catch (final UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
		{
			// file request
			entity = new FileEntity(file);
			
			String extension = "";

			final int i = path.toString().lastIndexOf('.');
			if (i > 0)
			{
				extension = path.toString().substring(i + 1);
			}
			System.err.println(extension);
			
			final String contentType = extension2contentType.get(extension);
			response.addHeader(HTTP.CONTENT_TYPE, contentType);
		}
		response.setEntity(entity);
		
		return file.isFile();
	}
	
	
	private void handleRequest(RequestObject reqObj)
	{
		final Path path = reqObj.getPath();
		final HttpServerConnection conn = reqObj.getConn();
		final HttpRequest req = reqObj.getRequest();
		
		WebServerLog.log(
			this,
			"Request handler got a request:\n" + req.toString());
		
		final HttpResponse response =
			new BasicHttpResponse(
				HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK,
				ReasonPhrases.OK);
		
		boolean applyFilters;
		applyFilters = handleFileRequest(path, response);
		
		try
		{
			proc.process(response, new BasicHttpContext());
		} catch (HttpException | IOException e1)
		{
			WebServerLog.log(
				this,
				"Request handler failed to 'process' response");
			e1.printStackTrace();
		}
		
		if (applyFilters)
		{
			// TODO fix call to filterChain
			filterChain.reset(path.toString());
			filterChain.doFilter(reqObj.getRequest(), response);
		}
		try
		{
			proc.process(response, new BasicHttpContext());
		} catch (HttpException | IOException e1)
		{
			WebServerLog.log(
				this,
				"Request handler failed to 'process' response after filtering");
			e1.printStackTrace();
		}
		WebServerLog.log(this, "Request handler is sending a response:\n"
			+ response.toString());
		
		try
		{
			conn.sendResponseHeader(response);
			conn.sendResponseEntity(response);
		} catch (HttpException | IOException e)
		{
			WebServerLog.log(
				this,
				"Request handler has encountered error while sending response");
			e.printStackTrace();
		}
	}



	// Timer timer = new Timer(true);

	private final String baseDir;
	
	private final LinkedBlockingQueue<RequestObject> requestsQueue;
	
	private final Map<String, String> extension2contentType;
	
	private final FilterChainImpl filterChain;
	
	private HttpProcessor proc;
}
