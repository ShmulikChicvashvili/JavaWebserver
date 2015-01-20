/**
 *
 */

package il.technion.cs236369.webserver.simplefilter;

import il.technion.cs236369.webserver.WebServerLog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

/**
 * @author Shmulik
 *
 */
public class GZipSimpleFilter implements SimpleFilter {

	@SuppressWarnings("nls")
	public static String decompress(byte[] bytes) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		final GZIPInputStream gis = new GZIPInputStream(
				new ByteArrayInputStream(bytes));
		final BufferedReader bf = new BufferedReader(new InputStreamReader(gis,
				"UTF-8"));
		String outStr = "";
		String line;
		while ((line = bf.readLine()) != null) {
			outStr += line;
		}
		return outStr;
	}

	@SuppressWarnings("nls")
	private static byte[] compress(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		final ByteArrayOutputStream obj = new ByteArrayOutputStream();
		try (final GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
			gzip.write(str.getBytes("UTF-8"));
		} catch (final IOException e) {
			System.err.println("Failed to create GZIPOutputSteam");
			e.printStackTrace();
		}
		return obj.toByteArray();
	}

	private static boolean isAcceptingGzip(HttpRequest request) {
		if (request.containsHeader(HttpHeaders.ACCEPT_ENCODING)
				&& request.getHeaders(HttpHeaders.ACCEPT_ENCODING)[0]
						.getValue().contains("gzip")) {
			return true;
		}
		return false;
	}

	private static boolean isAllreadyGzipped(HttpResponse response) {
		if (response.containsHeader(HttpHeaders.CONTENT_ENCODING)
				&& response.getFirstHeader(HttpHeaders.CONTENT_ENCODING)
						.getValue().equals("gzip")) {
			return true;
		}
		return false;
	}

	private static void setGzipHeader(HttpResponse response) {
		if (response.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
			response.removeHeaders(HttpHeaders.CONTENT_ENCODING);
		}
		response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
	}

	/*
	 * (non-Javadoc) @see
	 * il.technion.cs236369.webserver.simplefilter.SimpleFilter
	 * #doFilter(org.apache.http.HttpRequest, org.apache.http.HttpResponse,
	 * il.technion.cs236369.webserver.simplefilter.FilterChain)
	 */
	@Override
	public void doFilter(HttpRequest request, HttpResponse response,
			FilterChain chain) {
		WebServerLog.log(this, "GZipFilter is zipping a response!!!");
		HttpEntity entity = response.getEntity();

		if (!isAcceptingGzip(request) || isAllreadyGzipped(response)
				|| entity == null) {
			chain.doFilter(request, response);
		}

		try {
			final byte[] ent = compress(EntityUtils.toString(entity));

			WebServerLog.log(this, "Decompressed message: " + decompress(ent));

			if (ent == null) {
				chain.doFilter(request, response);
			}

			entity = new ByteArrayEntity(ent);

			EntityUtils.updateEntity(response, entity);

			setGzipHeader(response);
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Error occurred while zipping the response");
		}

		chain.doFilter(request, response);
	}
}
