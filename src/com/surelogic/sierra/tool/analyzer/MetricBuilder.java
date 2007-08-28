package com.surelogic.sierra.tool.analyzer;

public interface MetricBuilder {

	MetricBuilder path(String path);

	MetricBuilder packageName(String name);

	MetricBuilder className(String name);

	MetricBuilder lineOfCode(int line);

	void build();
}
