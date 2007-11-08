package com.surelogic.sierra.metrics.output;

import java.io.PrintWriter;

import com.surelogic.sierra.metrics.model.Metrics;

public final class MetricsResultsGenerator {

	/**
	 * Output encoding.
	 */
	public static final String ENCODING = "UTF-8";

	private static final String METRICS = "metrics";
	private static final String CU = "class";
	private static final String NAME = "name";
	private static final String PATH = "path";
	private static final String PACKAGE = "package";
	private static final String LOC = "loc";

	private final PrintWriter f_out;
	private String f_indent = "";

	private void o(final String s) {
		f_out.print(f_indent);
		f_out.println(s);
	}

	/** The default name of package for files in root folder */
	public static final String DEFAULT_PACKAGE_PARENTHESIS = "(default package)";

	public MetricsResultsGenerator(final PrintWriter out) {
		assert out != null;
		f_out = out;
		o("<?xml version='1.0' encoding='" + ENCODING + "' standalone='yes'?>");
		o("<" + METRICS + ">");
	}

	public void write(final Metrics metrics) {
		final StringBuilder b = new StringBuilder();
		b.append("<").append(CU);
		Entities.addAttribute(NAME, metrics.getClassName(), b);
		if (metrics.getPackageName() == null
				|| "".equals(metrics.getPackageName())) {
			Entities.addAttribute(PACKAGE, DEFAULT_PACKAGE_PARENTHESIS, b);
		} else {
			Entities.addAttribute(PACKAGE, metrics.getPackageName(), b);
		}
		Entities.addAttribute(LOC, String.valueOf(metrics.getLoc()), b);
		Entities.addAttribute(PATH, metrics.getPath(), b);
		b.append("/>");
		o(b.toString());
	}

	public void close() {
		o("</" + METRICS + ">");
		f_out.close();
	}
}
