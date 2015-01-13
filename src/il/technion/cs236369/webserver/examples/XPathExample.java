package il.technion.cs236369.webserver.examples;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathExample {
	public static void main(String[] args) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder builder = docFactory.newDocumentBuilder();
		Document doc = builder.parse("config.xml");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		NodeList nl = (NodeList) xpath.compile("//mime/mime-mapping").evaluate(
				doc, XPathConstants.NODESET);
		System.out.println(nl.getLength());

		for (int i = 0; i < nl.getLength(); ++i) {
			String extension = xpath.compile("./extension")
					.evaluate(nl.item(i));
			String mime_type = xpath.compile("./mime-type")
					.evaluate(nl.item(i));
			System.out.println(extension + ",  " + mime_type);
		}
	}
}
