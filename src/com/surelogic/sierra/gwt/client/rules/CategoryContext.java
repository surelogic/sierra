package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;

public final class CategoryContext {
	private static final String PARAM_CATEGORY = "category";
	private static final String PARAM_FINDING = "finding";
	private Context context;

	public CategoryContext(Context context) {
		super();
		this.context = context;
	}

	public CategoryContext(Category category) {
		super();
		this.context = ContextManager.getContext();
		setFinding("");
		setCategory(category.getUuid());
	}

	public CategoryContext(FilterEntry finding) {
		super();
		this.context = ContextManager.getContext();
		setFinding(finding.getUuid());
		setCategory("");
	}

	public void updateContext() {
		ContextManager.setContext(context);
	}

	public Context getContext() {
		return context;
	}

	public String getCategory() {
		return context.getParameter(PARAM_CATEGORY);
	}

	public CategoryContext setCategory(String uuid) {
		return setParameter(PARAM_CATEGORY, uuid);
	}

	public String getFinding() {
		return context.getParameter(PARAM_FINDING);
	}

	public CategoryContext setFinding(String uuid) {
		return setParameter(PARAM_FINDING, uuid);
	}

	private CategoryContext setParameter(String name, String value) {
		Map params = new HashMap();
		params.put(name, value);
		context = Context.create(context, params);
		return this;
	}
}
