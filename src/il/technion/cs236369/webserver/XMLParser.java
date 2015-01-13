/**
 *
 */

package il.technion.cs236369.webserver;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;




/**
 * @author Shmulik
 *
 */
public class XMLParser
{
	public XMLParser()
		throws ParserConfigurationException,
		SAXException,
		IOException
	{
		final DocumentBuilderFactory docFactory =
			DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		final DocumentBuilder builder = docFactory.newDocumentBuilder();
		doc = builder.parse("config.xml");

		final XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
	}


	@SuppressWarnings("nls")
	Map<String, String> getMimeTypes()
	{
		final Map<String, String> $ = new HashMap<>();

		NodeList nl;
		try
		{
			nl =
				(NodeList) xpath.compile("//mime/mime-mapping").evaluate(
					doc,
					XPathConstants.NODESET);

			System.out.println(nl.getLength());

			for (int i = 0; i < nl.getLength(); ++i)
			{
				final String extension =
					xpath.compile("./extension").evaluate(nl.item(i));
				final String mime_type =
					xpath.compile("./mime-type").evaluate(nl.item(i));
				$.put(extension, mime_type);
			}

		} catch (final XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return $;
	}
	
	
	
	XPath xpath;

	Document doc;
}
