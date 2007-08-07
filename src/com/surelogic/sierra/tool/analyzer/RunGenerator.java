package com.surelogic.sierra.tool.analyzer;

public interface RunGenerator {
	
	RunGenerator javaVersion(String version);
	
	RunGenerator javaVendor(String vendor);
	
	RunGenerator project(String projectName);
	
	ArtifactGenerator build();
}
