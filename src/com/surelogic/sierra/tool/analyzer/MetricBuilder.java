package com.surelogic.sierra.tool.analyzer;

public interface MetricBuilder {

	MetricBuilder packageName(String name);

	MetricBuilder className(String name);

	MetricBuilder linesOfCode(int lines);

	void build();
}
