package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;
import com.surelogic.sierra.tool.config.Config;

/**
 * PublishMessageRunGenerator allows a client to build a run message, and
 * publish it to a remote server.
 * 
 * @author nathan
 * 
 */
public class PublishMessageRunGenerator implements ScanGenerator {

	private String javaVendor;
	private String javaVersion;
	private String project;
	private List<String> qualifiers;
	private String uid;

	public PublishMessageRunGenerator() {
	}

	public ArtifactGenerator build() {
		Scan r = new Scan();
		r.setUid(uid);
		Config config = new Config();
		config.setJavaVendor(javaVendor);
		config.setJavaVersion(javaVersion);
		config.setProject(project);
		config.setQualifiers(qualifiers);
		r.setConfig(config);
		return new PublishMessageArtifactGenerator(r);
	}

	public ScanGenerator javaVendor(String vendor) {
		this.javaVendor = vendor;
		return this;
	}

	public ScanGenerator javaVersion(String version) {
		this.javaVersion = version;
		return this;
	}

	public ScanGenerator project(String projectName) {
		this.project = projectName;
		return this;
	}

	public ScanGenerator qualifiers(Collection<String> qualifiers) {
		if (qualifiers != null) {
			this.qualifiers = new ArrayList<String>(qualifiers);
		}
		return this;
	}

	public ScanGenerator uid(String uid) {
		this.uid = uid;
		return this;
	}

	public ScanGenerator user(String userName) {
		// We do nothing here
		return this;
	}

}
