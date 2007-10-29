package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.SierraTool;
import com.surelogic.sierra.tool.config.Config;

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

	public Map<Config, File> writeBuildFiles(List<Config> configs,
			boolean override, String fileName) {
		Map<Config, File> buildFiles = new HashMap<Config, File>();
		f_fileName = fileName;
		for (Config c : configs) {
			File holder = writeBuildFile(c, override);
			if (holder != null) {
				buildFiles.put(c, holder);
			}
		}

		return buildFiles;
	}

	private File writeBuildFile(Config config, boolean override) {

		boolean wasCreated = false;
		String completeFileName;
		if (f_fileName != null) {
			completeFileName = config.getBaseDirectory() + File.separator
					+ f_fileName;
		} else {
			completeFileName = config.getBaseDirectory() + File.separator
					+ SierraConstants.SIERRA_BUILD_FILE;
		}
		String toolDirectory = getToolsDirectory();

		// FIXME: The implementation below assumes that the common project is at
		// the same level as the sierra-tool project
		File commonFolder = new File(toolDirectory);
		String commonDirectory = File.separator + commonFolder.getParent()
				+ File.separator + "common" + File.separator;

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
						+ SierraConstants.SIERRA_TOOL_SRC);
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
				attributeMap.put("resource", SierraConstants.ANTLIB_DIR);
				writeAttributes(attributeMap);
				hd.startElement("", "", "taskdef", atts);

				hd.startElement("", "", "classpath", null);

				// Dirset to include everything in project
				attributeMap.put("dir", SierraConstants.TOOL_PROPERTY);
				attributeMap.put("includes", SierraConstants.INCLUDE_ALL);
				writeAttributes(attributeMap);
				hd.startElement("", "", "dirset", atts);
				hd.endElement("", "", "dirset");

				// Dirset to include the common project
				attributeMap.put("dir", commonDirectory);
				attributeMap.put("includes", SierraConstants.INCLUDE_ALL);
				writeAttributes(attributeMap);
				hd.startElement("", "", "dirset", atts);
				hd.endElement("", "", "dirset");

				// Backport util concurrent (for FindBugs)
				attributeMap.put("dir", SierraConstants.TOOL_PROPERTY
						+ SierraConstants.BUC_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", SierraConstants.INCLUDE_ALL_JARS);
				writeAttributes(attributeMap);
				hd.startElement("", "", "include", atts);
				hd.endElement("", "", "include");
				hd.endElement("", "", "fileset");

				// JAXB file set
				attributeMap.put("dir", SierraConstants.TOOL_PROPERTY
						+ SierraConstants.JAX_LIB_LOCATION);
				writeAttributes(attributeMap);
				hd.startElement("", "", "fileset", atts);
				attributeMap.put("name", SierraConstants.INCLUDE_ALL_JARS);
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

	/**
	 * Utility to get the plugin directory
	 * 
	 * @return
	 */
	public static String getToolsDirectory() {
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

	@Deprecated
	public File writeMultipleProjectBuildFile(List<File> buildFiles) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String resultsFolder = tmpDir + File.separator
				+ SierraConstants.SIERRA_RESULTS;
		String fileName = resultsFolder + File.separator
				+ SierraConstants.MULTIPLE_SIERRA_PROJECTS;
		String toolDirectory = getToolsDirectory();
		File buildFile = new File(fileName);

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
			attributeMap.put("default", "Sierra-Ant-Multiple");
			attributeMap.put("basedir", toolDirectory
					+ SierraConstants.SIERRA_TOOL_SRC);
			attributeMap.put("name", "SIERRA");
			writeAttributes(attributeMap);
			hd.startElement("", "", "project", atts);

			// Include antlib.xml
			attributeMap.put("resource", SierraConstants.ANTLIB_DIR);
			writeAttributes(attributeMap);
			hd.startElement("", "", "taskdef", atts);

			hd.startElement("", "", "classpath", null);

			// Dirset to include everything in project
			attributeMap.put("dir", toolDirectory);
			attributeMap.put("includes", SierraConstants.INCLUDE_ALL);
			writeAttributes(attributeMap);
			hd.startElement("", "", "dirset", atts);
			hd.endElement("", "", "dirset");

			// // Findbugs fileset
			// attributeMap.put("dir", toolDirectory + FINDBUGS_LIB_LOCATION);
			// writeAttributes(attributeMap);
			// hd.startElement("", "", "fileset", atts);
			// attributeMap.put("name", FINDBUGS_JAR);
			// writeAttributes(attributeMap);
			// hd.startElement("", "", "include", atts);
			// hd.endElement("", "", "include");
			// hd.endElement("", "", "fileset");
			//
			// // PMD fileset location
			// attributeMap.put("dir", toolDirectory + PMD_40_LIB_LOCATION);
			// writeAttributes(attributeMap);
			// hd.startElement("", "", "fileset", atts);
			// attributeMap.put("name", INCLUDE_ALL_JARS);
			// writeAttributes(attributeMap);
			// hd.startElement("", "", "include", atts);
			// hd.endElement("", "", "include");
			// hd.endElement("", "", "fileset");

			// Backport util concurrent (for FindBugs)
			attributeMap.put("dir", toolDirectory
					+ SierraConstants.BUC_LIB_LOCATION);
			writeAttributes(attributeMap);
			hd.startElement("", "", "fileset", atts);
			attributeMap.put("name", SierraConstants.INCLUDE_ALL_JARS);
			writeAttributes(attributeMap);
			hd.startElement("", "", "include", atts);
			hd.endElement("", "", "include");
			hd.endElement("", "", "fileset");

			// JAXB file set
			attributeMap.put("dir", toolDirectory
					+ SierraConstants.JAX_LIB_LOCATION);
			writeAttributes(attributeMap);
			hd.startElement("", "", "fileset", atts);
			attributeMap.put("name", SierraConstants.INCLUDE_ALL_JARS);
			writeAttributes(attributeMap);
			hd.startElement("", "", "include", atts);
			hd.endElement("", "", "include");
			hd.endElement("", "", "fileset");

			// End taskdef
			hd.endElement("", "", "classpath");
			hd.endElement("", "", "taskdef");

			// Start target tag
			attributeMap.put("name", "Sierra-Ant-Multiple");
			writeAttributes(attributeMap);
			hd.startElement("", "", "target", atts);

			Iterator<File> buildFileIterator = buildFiles.iterator();

			while (buildFileIterator.hasNext()) {

				File buildFileHolder = buildFileIterator.next();
				attributeMap.put("antfile", buildFileHolder.getAbsolutePath());
				attributeMap.put("target", "Sierra-Ant");
				writeAttributes(attributeMap);
				hd.startElement("", "", "ant", atts);
				hd.endElement("", "", "ant");

			}

			// Close tags
			hd.endElement("", "", "target");
			hd.endElement("", "", "project");
			hd.endDocument();
			fos.close();

		} catch (SAXException se) {
			log.info("SAX Exception while writing build file " + se);
		} catch (IOException ioe) {
			log.info("I/O Exception while writing build file " + ioe);
		}

		return buildFile;
	}
}
