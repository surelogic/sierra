package com.surelogic.sierra.tool.message;

import static javax.xml.stream.XMLStreamConstants.*;

import java.io.*;
import java.util.Map;

import javax.xml.bind.*;
import javax.xml.stream.*;

import com.surelogic.sierra.tool.targets.*;

public class TestConfigUnderJava7 {
	public static void main(String[] args) throws Exception {
		Config c = readConfig(System.out, "./testConfig.xml");
		for (Map.Entry<String,String> e : c.getPluginDirs().entrySet()) {
			System.out.println(e.getKey()+" -> "+e.getValue());
		}
	}
	
	static Config readConfig(PrintStream out, String configName) throws Exception {
		FileInputStream file = new FileInputStream(configName);
		out.println("Got file: " + configName);		
		
		JAXBContext ctx = JAXBContext.newInstance(Config.class,
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
		return config;
	}
}
