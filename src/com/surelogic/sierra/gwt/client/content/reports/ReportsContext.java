package com.surelogic.sierra.gwt.client.content.reports;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Report;

public class ReportsContext {
	private static final String PARAM_REPORT = "report";
	private Context context;

	public ReportsContext(Context context) {
		super();
		this.context = context;
	}

	public ReportsContext(String reportUuid) {
		super();
		this.context = ContextManager.getContext();
		setReport(reportUuid);
	}

	public ReportsContext(Report report) {
		super();
		this.context = ContextManager.getContext();
		setReport(report.getUuid());
	}

	public void updateContext() {
		ContextManager.setContext(context);
	}

	public Context getContext() {
		return context;
	}

	public String getReport() {
		return context.getParameter(PARAM_REPORT);
	}

	public ReportsContext setReport(String uuid) {
		return setParameter(PARAM_REPORT, uuid);
	}

	private ReportsContext setParameter(String name, String value) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(name, value);
		context = Context.create(context, params);
		return this;
	}
}
