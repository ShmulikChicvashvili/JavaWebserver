package il.technion.cs236369.webserver;

import java.io.IOException;

public interface IWebServer {
	
	/**
	 * Create a new bounded server socket using ServerSocketFactory with the
	 * given port
	 * 
	 * @throws IOException
	 *             unable to bind the server socket
	 */
	public void bind() throws IOException;
	
	/**
	 * Starts the server loop: listens to client requests and executes them.
	 */
	public void start();

}
