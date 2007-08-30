package com.surelogic.sierra.metrics.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.surelogic.sierra.metrics.model.Metrics;

public final class MetricsResultsGenerator {

	private static AttributesImpl atts;
	private static FileOutputStream fos;
	private static OutputFormat of;
	private static XMLSerializer serializer;
	private static ContentHandler hd;

	private static void writeAttributes(Map<String, String> attributeMap) {

		atts.clear();

		Set<String> attributes = attributeMap.keySet();

		Iterator<String> attributesIterator = attributes.iterator();

		while (attributesIterator.hasNext()) {
			String attribute = attributesIterator.next();
			String value = attributeMap.get(attribute);

			atts.addAttribute("", "", attribute, "CDATA", value);
		}

		attributeMap.clear();

	}

	public static void startResultsFile(String outputFile) {
		File resultFile = new File(outputFile);

		try {
			fos = new FileOutputStream(resultFile);
			of = new OutputFormat("XML", "ISO-8859-1", true);
			of.setIndent(1);
			of.setIndenting(true);
			of.setOmitXMLDeclaration(true);
			serializer = new XMLSerializer(fos, of);
			// SAX2.0 ContentHandler.
			hd = serializer.asContentHandler();
			// hd.startDocument();
			atts = new AttributesImpl();
			// Start the file
			hd.startElement("", "", "metrics", atts);

		} catch (SAXException se) {
			System.out.println("SAX Exception while writing build file " + se);
		} catch (IOException ioe) {
			System.out.println("I/O Exception while writing build file " + ioe);

		}

	}

	public static void writeInFile(Metrics metrics) {
		try {
			// Start project tag
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("name", metrics.getClassName());
			attributeMap.put("package", metrics.getPackageName());
			attributeMap.put("loc", String.valueOf(metrics.getLoc()));
			attributeMap.put("path", metrics.getPath());
			writeAttributes(attributeMap);
			hd.startElement("", "", "class", atts);
			hd.endElement("", "", "class");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void endResultsFile() {

		try {
			hd.endElement("", "", "metrics");
			fos.close();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
