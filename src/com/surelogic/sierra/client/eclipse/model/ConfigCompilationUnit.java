package com.surelogic.sierra.client.eclipse.model;

import java.util.List;
import java.util.Map;

import com.surelogic.sierra.tool.message.Config;

public class ConfigCompilationUnit {

	private Config f_config;

	private Map<String, List<String>> f_packageCompilationUnitMap;

	public ConfigCompilationUnit(Config config,
			Map<String, List<String>> packageCompilationUnitMap) {
		f_config = config;
		f_packageCompilationUnitMap = packageCompilationUnitMap;

	}

	public Config getConfig() {
		return f_config;
	}

	public void setConfig(Config config) {
		this.f_config = config;
	}

	public Map<String, List<String>> getPackageCompilationUnitMap() {
		return f_packageCompilationUnitMap;
	}

	public void setPackageCompilationUnitMap(
			Map<String, List<String>> packageCompilationUnitMap) {
		this.f_packageCompilationUnitMap = packageCompilationUnitMap;
	}

}
