package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Config;

public class BuildFileGenerator {

	private static final Logger log = SLLogger.getLogger("sierra");
	private AttributesImpl atts;
	private static final BuildFileGenerator INSTANCE = new BuildFileGenerator();
	private String f_fileName;

	public static BuildFileGenerator getInstance() {
		return INSTANCE;
	}

	public BuildFileGenerator() {
		// Nothing to do
	}

	public Map<Config, File> writeBuildFiles(String toolDirectory,
			String commonDirectory, List<Config> configs, boolean override,
			String fileName) {
		Map<Config, File> buildFiles = new HashMap<Config, File>();
		f_fileName = fileName;
		for (Config c : configs) {
			File holder = writeBuildFile(toolDirectory, commonDirectory, c,
					override);
			if (holder != null) {
				buildFiles.put(c, holder);
			}
		}
		return buildFiles;
	}

	private File writeBuildFile(String toolDirectory, String commonDirectory,
			Config config, boolean override) {

		boolean wasCreated = false;
		String completeFileName;
		if (f_fileName != null) {
			completeFileName = config.getBaseDirectory() + File.separator
					+ f_fileName;
		} else {
			completeFileName = config.getBaseDirectory() + File.separator
					+ SierraToolConstants.SIERRA_BUILD_FILE;
		}

		// For Windows machines - Not required only for consistency
		commonDirectory = commonDirectory.replace(File.separator, "/");

		File buildFile = new File(completeFileName);

		if (!buildFile.exists() || override) {

			try {
				FileOutputStream fos = new FileOutputStream(completeFileName);
				OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
				of.setIndent(1);
				of.setIndenting(true);
				XMLSerializer serializer = new XMLSerializer(fos, of);

				// ContentHandler.
				ContentHandler hd = serializer.asContentHandler();
				hd.startDocument();
				atts = new AttributesImpl();

				// Start the task
				Map<String, String> attributeMap = new HashMap<String, String>();
				attributeMap.put("default", "Sierra-Ant");
				attributeMap.put("basedir", toolDirectory
						+ SierraToolConstants.SIERRA_TOOL_SRC);
				attributeMap.put("name", "SIERRA");
				writeAttributes(attributeMap);
				hd.startElement("", "", "project", atts);

				// Create property
				attributeMap.put("name", "tool");
				attributeMap.put("location", toolDirectory);
				writeAttributes(attributeMap);
				hd.startElement("", "", "property", atts);
				hd.endElement("", "", "property");

				// Include antlib.xml
				attributeMap.put("resource", SierraToolConstants.ANTLIB_DIR);
				writeAttributes(attributeMap);
				hd.startElement("", "", "taskdef", atts);

				hd.startElement("", "", "classpath", null);

				// Dirset to include everything in project
				attributeMap.put("dir", SierraToolConstants.TOOL_PROPERTY);
				attributeMap.put("includes", SierraToolConstants.INCLUDE_ALL);
				writeAttributes(attributeMap);
				hd.startElement("", "", "dirset", atts);
				hd.endElement("", "", "dirset");

				// Dirset to include the common project
				attributeMap.put("dir", commonDirectory);
				attributeMap.put("includes", SierraToolConstants.INCLUDE_ALL);
				writeAttributes(attributeMap);
				hd.startElement("", "", "dirset", atts);
				hd.endElement("", "", "dirset");

				// Backport util concurrent (for FindBugs)
				attributeMap.put("dir", SierraToolConstants.TOOL_PROPERTY
						+ SierraToolConstants.BUC_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", SierraToolConstants.INCLUDE_ALL_JARS);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// JAXB file set
				attributeMap.put("dir", SierraToolConstants.TOOL_PROPERTY
						+ SierraToolConstants.JAX_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", SierraToolConstants.INCLUDE_ALL_JARS);
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
				attributeMap.put("srcdir", config.getBaseDirectory()
						.getAbsolutePath());
				attributeMap.put("bindir", config.getBaseDirectory()
						.getAbsolutePath());
				attributeMap.put("runDocument", config.getScanDocument()
						.getAbsolutePath());
				writeAttributes(attributeMap);
				hd.startElement("", "", "sierra-analysis", atts);

				// Start project tag
				attributeMap.put("name", config.getProject());
				attributeMap.put("dir", config.getBaseDirectory()
						.getAbsolutePath());
				writeAttributes(attributeMap);
				hd.startElement("", "", "project", atts);

				// Close tags
				hd.endElement("", "", "project");
				hd.endElement("", "", "sierra-analysis");
				hd.endElement("", "", "target");
				hd.endElement("", "", "project");
				hd.endDocument();
				fos.close();

				wasCreated = true;

			} catch (SAXException se) {
				log.info("SAX Exception while writing build file " + se);
			} catch (IOException ioe) {
				log.info("I/O Exception while writing build file " + ioe);
			}

		}

		if (wasCreated) {
			return buildFile;
		}

		return null;
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
}
