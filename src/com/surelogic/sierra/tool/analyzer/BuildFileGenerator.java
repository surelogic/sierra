package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.SierraTool;
import com.surelogic.sierra.tool.config.Config;

public class BuildFileGenerator {

	private static final Logger log = SierraLogger.getLogger("Sierra");
	private static final String SIERRA_BUILD_FILE = "sierra-run.xml";
	private static final String ANTLIB_DIR = "com/surelogic/sierra/tool/ant/antlib.xml";
	private static final String SIERRA_TOOL_SRC = "src";
	private static final String INCLUDE_ALL = "**/*";
	private static final String FINDBUGS_LIB_LOCATION = "Tools/FB/lib";
	private static final String FINDBUGS_JAR = "findbugs.jar";
	private static final String PMD_LIB_LOCATION = "Tools/pmd-3.9/lib";
	private static final String INCLUDE_ALL_JARS = "**/*.jar";
	private static final String BUC_LIB_LOCATION = "Tools/backport-util-concurrent-3.0";
	private static final String JAX_LIB_LOCATION = "Tools/jax-ws";
	private Config config;
	private AttributesImpl atts;

	/**
	 * Entry.
	 */
	public static void main(String[] args) {
		BuildFileGenerator buildFileGenerator = new BuildFileGenerator(
				new Config());
		buildFileGenerator.writeBuildFile();
	}

	public BuildFileGenerator(Config config) {
		this.config = config;
	}

	public File writeBuildFile() {

		String fileName = config.getBaseDirectory() + File.separator
				+ SIERRA_BUILD_FILE;
		String toolDirectory = getToolsDirectory();

		File buildFile = new File(fileName);

		if (!buildFile.exists()) {

			try {
				FileOutputStream fos = new FileOutputStream(fileName);
				OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
				of.setIndent(1);
				of.setIndenting(true);
				XMLSerializer serializer = new XMLSerializer(fos, of);
				// SAX2.0 ContentHandler.
				ContentHandler hd = serializer.asContentHandler();
				hd.startDocument();
				atts = new AttributesImpl();

				// Start the task
				Map<String, String> attributeMap = new HashMap<String, String>();
				attributeMap.put("default", "Sierra-Ant");
				attributeMap.put("basedir", toolDirectory + SIERRA_TOOL_SRC);
				attributeMap.put("name", "SIERRA");
				writeAttributes(attributeMap);
				hd.startElement("", "", "project", atts);

				// Include antlib.xml
				attributeMap.put("resource", ANTLIB_DIR);
				writeAttributes(attributeMap);
				hd.startElement("", "", "taskdef", atts);

				hd.startElement("", "", "classpath", null);

				// Dirset to include everything in project
				attributeMap.put("dir", toolDirectory);
				attributeMap.put("includes", INCLUDE_ALL);
				writeAttributes(attributeMap);
				hd.startElement("", "", "dirset", atts);
				hd.endElement("", "", "dirset");

				// Findbugs fileset
				attributeMap.put("dir", toolDirectory + FINDBUGS_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", FINDBUGS_JAR);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// PMD fileset location
				attributeMap.put("dir", toolDirectory + PMD_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", INCLUDE_ALL_JARS);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// Backport util concurrent (for FindBugs)
				attributeMap.put("dir", toolDirectory + BUC_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", INCLUDE_ALL_JARS);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// JAXB file set
				attributeMap.put("dir", toolDirectory + JAX_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", INCLUDE_ALL_JARS);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// End taskdef
				hd.endElement("", "", "classpath");
				hd.endElement("", "", "taskdef");

				// Start target tag
				attributeMap.put("name", "Sierra-Ant");
				writeAttributes(attributeMap);
				hd.startElement("", "", "target", atts);

				// Start Sierra analysis tag
				attributeMap.put("destDir", config.getDestDirectory()
						.getAbsolutePath());
				attributeMap.put("srcdir", config.getBaseDirectory());
				attributeMap.put("bindir", config.getBaseDirectory());
				attributeMap.put("runDocument", config.getRunDocument()
						.getAbsolutePath());
				writeAttributes(attributeMap);
				hd.startElement("", "", "sierra-analysis", atts);

				// Start project tag
				attributeMap.put("name", config.getProject());
				attributeMap.put("dir", config.getBaseDirectory());
				writeAttributes(attributeMap);
				hd.startElement("", "", "project", atts);

				// Source tag
				// attributeMap.put("dir", config.getBaseDirectory());
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "source", atts);

				// attributeMap.put("name", "**/src");
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "include", atts);
				// hd.endElement("", "", "include");
				//
				// attributeMap.put("name", "**/Tools/**");
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "exclude", atts);
				// hd.endElement("", "", "exclude");
				// hd.endElement("", "", "source");

				// Binary tag
				// attributeMap.put("dir", config.getBaseDirectory());
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "binary", atts);

				// attributeMap.put("name", "**/bin");
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "include", atts);
				// hd.endElement("", "", "include");
				//
				// attributeMap.put("name", "**/Tools/**");
				// writeAttributes(attributeMap);
				// hd.startElement("", "", "exclude", atts);
				// hd.endElement("", "", "exclude");
				// hd.endElement("", "", "binary");

				// Close tags
				hd.endElement("", "", "project");
				hd.endElement("", "", "sierra-analysis");
				hd.endElement("", "", "target");
				hd.endElement("", "", "project");
				hd.endDocument();
				fos.close();

			} catch (SAXException se) {
				log.info("SAX Exception while writing build file " + se);
			} catch (IOException ioe) {
				log.info("I/O Exception while writing build file " + ioe);
			}

		}
		return buildFile;
	}

	private void writeAttributes(Map<String, String> attributeMap) {

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

	private String getToolsDirectory() {
		String commonDirectory = "";

		URL relativeURL = SierraTool.getDefault().getBundle().getEntry("");

		try {

			URL commonPathURL = FileLocator.resolve(relativeURL);
			commonDirectory = commonPathURL.getPath();

			return commonDirectory;

		} catch (IOException e) {
			log.log(Level.SEVERE, "Error getting plugin directory.", e);
		}

		return commonDirectory;
	}
}
