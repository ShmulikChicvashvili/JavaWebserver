package il.technion.cs236369.webserver;

import java.io.*;

public class WebServerLog {

	private static PrintStream _out = System.out;

	public static synchronized void log(Object obj, String message) {
		System.err.flush();
		_out.println("---------------------------");
		_out.print(obj.getClass().getName());
		_out.println(" [" + new java.util.Date() + "]:");
		_out.println(message);
		_out.println("---------------------------");
		_out.flush();
	}

	public static synchronized void setStream(PrintStream out) {
		_out = out;
	}
}