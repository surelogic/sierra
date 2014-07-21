/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.*;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

final class RemoteTool extends AbstractRemoteSLJob {
	public static void main(String[] args) {
		RemoteTool job = new RemoteTool();
		job.run();
	}
	
	@Override
	protected SLJob init(BufferedReader br, Monitor mon) throws Exception {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		out.println("Lowered thread priority");

		String configName = System
		.getProperty(SierraToolConstants.CONFIG_PROPERTY);
		if (configName == null) {
			throw new IllegalArgumentException("No config provided");
		}
		final File configFile = new File(configName);
		FileInputStream file = new FileInputStream(configFile);
		out.println("Got file: " + configName);

		JAXBContext ctx = JAXBContext.newInstance(Config.class, KeyValuePair.class,
				FileTarget.class, JarTarget.class,
				FullDirectoryTarget.class, FilteredDirectoryTarget.class);
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(file);
		out.println("Created reader");
		Unmarshaller unmarshaller = ctx.createUnmarshaller();

		xmlr.nextTag();
		out.println("Finding next tag");
		xmlr.require(START_ELEMENT, null, "config");
		out.println("Checking for config");

		final Config config = unmarshaller.unmarshal(xmlr, Config.class)
		.getValue();
		// Config config = (Config) unmarshaller.unmarshal(file);
		out.println("Read config");
		file.close();
		
		String configTxt = FileUtility.getFileContentsAsString(configFile);
		out.println(configTxt);
		configFile.delete();

		out.println("Plugin dirs:");
		for (Map.Entry<String,String> e : config.getPluginDirs().entrySet()) {
			out.println(e.getKey()+" -> "+e.getValue());
		}
		for (URI location : config.getPaths()) {
			out.println("URI = " + location);
		}
		for (ToolTarget t : config.getTargets()) {
			out.println(t.getType() + " = " + t.getLocation());
		}
		out.println("Excluded tools = "+config.getExcludedToolsList());
		out.println("Extensions ...");		
		for(ToolExtension te : config.getExtensions()) {
			out.println(te.getTool()+" has extension "+te.getId());
		}
		out.flush();

		addToolFinder(out, config);
		
		final IToolInstance ti = ToolUtil.create(out, config, false);
		out.println("Java version: " + config.getJavaVersion());
		out.println("Rules file: " + config.getPmdRulesFile());
		/*
		out.println("Classpath: "+System.getProperty("java.class.path"));
		for(ArtifactType at : t.getArtifactTypes()) {
			if ("PMD".equals(at.tool) && at.plugin.contains("de.bsd")) {
				out.println("Found "+at.type);
			}
		}
		*/
		checkInput(br, mon, "Created tool instance");
		return ti;
	}

	private void addToolFinder(PrintStream out, final Config config) {
		out.println("Setting up remote tool finder ... "+config.getPluginDirs().size());
		final List<File> dirs = new ArrayList<File>();
		for(Map.Entry<String, String> e : config.getPluginDirs().entrySet()) {
			final File f = new File(e.getValue());
			out.println("Checking if it's a tool plugin: "+f);
			if (ToolUtil.isToolPlugin(f)) {
				dirs.add(f);
			}
		}
		
		ToolUtil.addToolFinder(new IToolFinder() {
			public List<File> findToolDirectories() {
				return dirs;
			}			
		});
	}
}
