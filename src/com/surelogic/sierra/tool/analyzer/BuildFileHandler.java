package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DirSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.message.Config;

/**
 * XML parser for build files
 * 
 * @author Tanmay.Sinha
 * 
 */
public class BuildFileHandler extends DefaultHandler {

	private Config config = null;

	private List<Config> configs = new ArrayList<Config>();

	private String srcDir = "";

	private String binDir = "";

	private boolean inSierraAnalysis = false;

	private boolean inSource = false;
	private String currentSrcDir = "";
	private String srcInclude = "";
	private String srcExclude = "";

	private boolean inBinary = false;
	private String currentBinDir = "";
	private String binInclude = "";
	private String binExclude = "";

	private final Project antProject = new Project();

	@SuppressWarnings("unused")
	private final String javaVendor;
	@SuppressWarnings("unused")
	private final String javaVersion;

	private String runDocumentNameHolder;

	public BuildFileHandler() {
		javaVendor = System.getProperty("java.vendor");
		javaVersion = System.getProperty("java.version");
		config = new Config();
		configs.clear();

	}

	// private static final Logger log = SierraLogger.getLogger("Sierra");

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		String eName = localName;

		if ("".equals(eName)) {
			eName = qName;
		}

		if ("sierra-analysis".equals(eName)) {
			inSierraAnalysis = true;
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("destDir".equals(aName)) {
						File destDir = new File(attributes.getValue(i));
						config.setDestDirectory(destDir);
					}

					if ("srcdir".equals(aName)) {
						if ("".equals(srcDir)) {
							srcDir = attributes.getValue(i);
						} else {
							srcDir = srcDir + File.pathSeparator
									+ attributes.getValue(i);
						}
					}

					if ("bindir".equals(aName)) {
						if ("".equals(binDir)) {
							binDir = attributes.getValue(i);
						} else {
							binDir = binDir + File.pathSeparator
									+ attributes.getValue(i);
						}
					}

					if ("runDocument".equals(aName)) {
						runDocumentNameHolder = attributes.getValue(i);

					}

					if ("Clean".equals(aName)) {
						String cleanHolder = attributes.getValue(i);
						boolean clean = Boolean.parseBoolean(cleanHolder);
						config.setCleanTempFiles(clean);
					}

				}
			}
		}

		if (("project".equals(eName)) && (inSierraAnalysis)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						String project = attributes.getValue(i);
						config.setProject(project);
					}

					if ("dir".equals(aName)) {
						File baseDirectory = new File(attributes.getValue(i));
						config.setBaseDirectory(baseDirectory);
					}

				}
			}
		}

		// GOING IN SOURCE TAG FOR PROJECT

		if (("source".equals(eName)) && (inSierraAnalysis)) {

			inSource = true;
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("dir".equals(aName)) {
						currentSrcDir = attributes.getValue(i);
					}

				}
			}
		}

		if (("include".equals(eName)) && (inSource)) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						srcInclude = attributes.getValue(i);
					}

				}
			}
		}

		if (("exclude".equals(eName)) && (inSource)) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						srcExclude = attributes.getValue(i);
					}

				}
			}
		}

		// GOING IN BINARY TAG FOR PROJECT

		if (("binary".equals(eName)) && (inSierraAnalysis)) {

			inBinary = true;
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("dir".equals(aName)) {
						currentBinDir = attributes.getValue(i);
					}

				}
			}
		}

		if (("include".equals(eName)) && (inBinary)) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						binInclude = attributes.getValue(i);
					}

				}
			}
		}

		if (("exclude".equals(eName)) && (inBinary)) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("name".equals(aName)) {
						binExclude = attributes.getValue(i);
					}

				}
			}
		}

		if (("tools".equals(eName)) && (inSierraAnalysis)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("exclude".equals(aName)) {
						String excludedToolsList = attributes.getValue(i);
						config.setExcludedToolsList(excludedToolsList);
					}

					if ("multithreaded".equals(aName)) {
						String holder = attributes.getValue(i);
						boolean multithreaded = Boolean.parseBoolean(holder);
						config.setMultithreaded(multithreaded);
					}

					if ("toolsFolder".equals(aName)) {
						String holder = attributes.getValue(i);
						File toolsDirectory = new File(holder);
						config.setToolsDirectory(toolsDirectory);
					}

				}
			}
		}

		if (("pmdconfig".equals(eName)) && (inSierraAnalysis)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					if ("javaVersion".equals(aName)) {
						String javaVersion = attributes.getValue(i);
						config.setJavaVersion(javaVersion);
					}

					if ("rulefile".equals(aName)) {
						String holder = attributes.getValue(i);
						File pmdRulesFile = new File(holder);
						config.setPmdRulesFile(pmdRulesFile);
					}

				}
			}
		}

		if (("findbugsconfig".equals(eName)) && (inSierraAnalysis)) {

			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i);
					if ("".equals(aName)) {
						aName = attributes.getQName(i);
					}

					// TODO: Add memory and rule file support to findbugs
					if ("memory".equals(aName)) {
						// String javaVersion = attributes.getValue(i);
						// config.set(javaVersion);
					}

				}
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		String eName = localName;

		if ("".equals(eName)) {
			eName = qName;
		}

		if ("sierra-analysis".equals(eName)) {
			inSierraAnalysis = false;

			File scanDocument = new File(runDocumentNameHolder);
			if (scanDocument == null || "".equals(runDocumentNameHolder)) {
				scanDocument = new File(SierraToolConstants.SIERRA_RESULTS_PATH,
						config.getProject()
								+ SierraToolConstants.PARSED_FILE_SUFFIX);
			} else if (scanDocument.isDirectory()) {
				scanDocument = new File(scanDocument, config.getProject()
						+ SierraToolConstants.PARSED_FILE_SUFFIX);
			} else if (!scanDocument.getName().endsWith(
					SierraToolConstants.PARSED_FILE_SUFFIX)) {
				scanDocument = new File(scanDocument.getParentFile(),
						scanDocument.getName()
								+ SierraToolConstants.PARSED_FILE_SUFFIX);
			}
			config.setScanDocument(scanDocument);
			config.setRunDateTime(Calendar.getInstance().getTime());
			config.setSourceDirs(srcDir);
			config.setBinDirs(binDir);
			config.setJavaVendor(javaVendor);
			configs.add(config);

			resetParameters();

		}

		if ("source".equals(eName)) {
			inSource = false;
			String dirs = getDirectories(currentSrcDir, srcInclude, srcExclude);
			if (srcDir.equals("")) {
				srcDir = dirs;
			} else {
				srcDir = srcDir + File.pathSeparator + dirs;
			}
		}

		if ("binary".equals(eName)) {
			inBinary = false;
			String dirs = getDirectories(currentBinDir, binInclude, binExclude);
			if (binDir.equals("")) {
				binDir = dirs;
			} else {
				binDir = binDir + File.pathSeparator + dirs;
			}
		}
	}

	private void resetParameters() {
		config = new Config();

		srcDir = "";
		binDir = "";

		currentSrcDir = "";
		srcInclude = "";
		srcExclude = "";

		currentBinDir = "";
		binInclude = "";
		binExclude = "";

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	private String getDirectories(String currentDir, String includes,
			String excludes) {

		DirSet dirset = new DirSet();
		dirset.setProject(antProject);
		dirset.setDir(new File(currentDir));
		dirset.setIncludes(includes);
		dirset.setExcludes(excludes);
		DirectoryScanner ds = dirset.getDirectoryScanner(antProject);
		String dirs[] = ds.getIncludedDirectories();

		String holder = "";

		// TODO: Add support for multiple <include> and <exclude> tags withing
		// same source tag also might want to reconsider using "/"

		currentDir += "/";
		for (String s : dirs) {
			s = s.replace("\\", "/");
			if (holder.equals("")) {
				holder += currentDir + s;
			} else {

				holder = holder + File.pathSeparator + currentDir + s;
			}
		}

		return holder;

	}

	public List<Config> getConfigs() {
		return configs;
	}
}
