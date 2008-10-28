/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.*;
import java.net.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.jobs.*;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

public class RemoteTool extends AbstractRemoteSLJob {
	public static void main(String[] args) {
		RemoteTool job = new RemoteTool();
		job.run();
	}
	
	@Override
	protected SLJob init(BufferedReader br, Monitor mon) throws Exception {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		System.out.println("Lowered thread priority");

		String configName = System
		.getProperty(SierraToolConstants.CONFIG_PROPERTY);
		if (configName == null) {
			throw new IllegalArgumentException("No config provided");
		}
		FileInputStream file = new FileInputStream(configName);
		System.out.println("Got file: " + configName);

		JAXBContext ctx = JAXBContext.newInstance(Config.class,
				FileTarget.class, JarTarget.class,
				FullDirectoryTarget.class, FilteredDirectoryTarget.class);
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(file);
		System.out.println("Created reader");
		Unmarshaller unmarshaller = ctx.createUnmarshaller();

		xmlr.nextTag();
		System.out.println("Finding next tag");
		xmlr.require(START_ELEMENT, null, "config");
		System.out.println("Checking for config");

		final Config config = unmarshaller.unmarshal(xmlr, Config.class)
		.getValue();
		// Config config = (Config) unmarshaller.unmarshal(file);
		System.out.println("Read config");
		file.close();
		new File(configName).delete();

		// String line = br.readLine();
		// while (line != null) {
		// if (line.equals("\n")) {
		// break;
		// }
		// System.out.println(line);
		// line = br.readLine();
		// }

		for (URI location : config.getPaths()) {
			System.out.println("URI = " + location);
		}
		for (ToolTarget t : config.getTargets()) {
			System.out.println(t.getType() + " = " + t.getLocation());
		}
		System.out.println("Excluded tools = "
				+ config.getExcludedToolsList());
		System.out.flush();

		final ITool t = ToolUtil.create(config, false);
		System.out.println("Java version: " + config.getJavaVersion());
		System.out.println("Rules file: " + config.getPmdRulesFile());

		final IToolInstance ti = t.create(config);
		checkInput(br, mon, "Created tool instance");
		return ti;
	}
}
