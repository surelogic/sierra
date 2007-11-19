package com.surelogic.sierra.tool.message;

public interface MetricBuilder {

	MetricBuilder packageName(String name);

	MetricBuilder compilation(String name);

	MetricBuilder linesOfCode(int lines);

	void build();
}
