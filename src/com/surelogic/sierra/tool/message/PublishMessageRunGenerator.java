package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;
import com.surelogic.sierra.tool.config.Config;

/**
 * PublishMessageRunGenerator allows a client to build a run message, and
 * publish it to a remote server.
 * 
 * @author nathan
 * 
 */
public class PublishMessageRunGenerator implements RunGenerator {

	private String javaVendor;
	private String javaVersion;
	private String project;

	public PublishMessageRunGenerator() {

	}

	public ArtifactGenerator build() {
		Run r = new Run();
		Config config = new Config();
		config.setJavaVendor(javaVendor);
		config.setJavaVersion(javaVersion);
		config.setProject(project);
		r.setConfig(config);
		return new PublishMessageArtifactGenerator(r);
	}

	public RunGenerator javaVendor(String vendor) {
		this.javaVendor = vendor;
		return this;
	}

	public RunGenerator javaVersion(String version) {
		this.javaVersion = version;
		return this;
	}

	public RunGenerator project(String projectName) {
		this.project = projectName;
		return this;
	}

}
