package com.surelogic.sierra.tool.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a general configuration setup for a tool. The Launcher will accept
 * objects of this class or its subclasses.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class BaseConfig implements ToolConfig {

	/**
	 * The absolute location of the project containing source, class and jar
	 * files
	 */
	private String baseDirectory;

	/** The relative location of the source directories of the project */
	private String[] sourceDirectories;

	/** The JDK version */
	private String jdkVersion;

	/** The name of the project */
	private String projectName;

	/** The location of tools */
	private String toolsDirectory;

	public static List<BaseConfig> generateToolConfigs(InputStream in) {
		BaseConfigHandler h = new BaseConfigHandler();
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(in, h);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return h.configs;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(String string) {
		this.baseDirectory = string;
	}

	public void setProjectName(String string) {
		this.projectName = string;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getJdkVersion() {
		return jdkVersion;
	}

	public void setJdkVersion(String string) {
		this.jdkVersion = string;
	}

	public String[] getSourceDirectories() {
		return sourceDirectories;
	}

	public void setSourceDirectories(String[] sourceDirectories) {
		this.sourceDirectories = sourceDirectories;

	}

	@Override
	public String toString() {
		return "Base Config: baseDirectory=" + baseDirectory
				+ ", sourceDirectories=" + sourceDirectories + ", jdkVersion="
				+ jdkVersion + ", projectName=" + projectName;
	}

	private static class BaseConfigHandler extends DefaultHandler {
		private List<BaseConfig> configs;

		private static final String SPS = "sps";
		private static final String PROJECT = "project";
		private static final String NAME = "name";
		private static final String BASE = "baseDirectory";
		private static final String SOURCE = "sourceDirectory";
		private static final String SOURCES = "sourceDirectories";
		private static final String LIBRARY = "library";
		private static final String JDK = "jdkVersion";

		private BaseConfig config;

		private List<String> sources = new ArrayList<String>();

		private StringBuilder builder = new StringBuilder();

		private static final String[] sourceArrayType = new String[0];

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			builder.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (PROJECT.equals(name)) {
				configs.add(config);
				config = null;
			} else if (NAME.equals(name)) {
				config.setProjectName(builder.toString());
			} else if (JDK.equals(name)) {
				config.setJdkVersion(builder.toString());
			} else if (BASE.equals(name)) {
				config.setBaseDirectory(builder.toString());
			} else if (SOURCE.equals(name)) {
				sources.add(builder.toString());
			} else if (SOURCES.equals(name)) {
				config.setSourceDirectories(sources.toArray(sourceArrayType));
			}

		}

		private static final List<String> FIELD_ELEMENTS = Arrays
				.asList(new String[] { NAME, BASE, SOURCE, LIBRARY, JDK });

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (SPS.equals(name)) {
				configs = new ArrayList<BaseConfig>();
			} else if (PROJECT.equals(name)) {
				config = new BaseConfig();
			} else if (FIELD_ELEMENTS.contains(name)) {
				builder.setLength(0);
			} else if (SOURCES.equals(name)) {
				sources.clear();
			}

		}
	}

	public String getToolsDirectory() {
		return toolsDirectory;
	}

	public void setToolsDirectory(String toolsDirectory) {
		this.toolsDirectory = toolsDirectory;
	}

}
