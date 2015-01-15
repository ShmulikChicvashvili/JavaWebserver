/**
 *
 */

package il.technion.cs236369.webserver;


import il.technion.cs236369.webserver.simplefilter.SimpleFilter;
import il.technion.cs236369.webserver.simplefilter.SimpleFilterWrapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
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


	public List<SimpleFilterWrapper> getFilterWrappers()
	{
		final List<SimpleFilterWrapper> $ = new ArrayList<>();

		NodeList nl;
		Node className;
		SimpleFilter filter = null;
		NodeList childNode;
		Set<String> urls = null;
		SimpleFilterWrapper filterWrapper;

		try
		{
			nl =
				(NodeList) xpath
					.compile("//simple-filters/simple-filter")
					.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++)
			{
				className = nl.item(i).getAttributes().getNamedItem("class");

				filter =
					(SimpleFilter) Class
						.forName(className.getNodeValue().toString())
						.getConstructor()
						.newInstance();

				childNode = nl.item(i).getChildNodes();
				urls = new HashSet<>();
				for (int j = 0; j < childNode.getLength(); j++)
				{
					final String url = childNode.item(j).getTextContent();
					urls.add(url);
				}
				filterWrapper = new SimpleFilterWrapper(filter, urls);
				$.add(filterWrapper);
			}

		} catch (final
			XPathExpressionException
			| InstantiationException
			| IllegalAccessException
			| IllegalArgumentException
			| InvocationTargetException
			| NoSuchMethodException
			| SecurityException
			| ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return $;
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
