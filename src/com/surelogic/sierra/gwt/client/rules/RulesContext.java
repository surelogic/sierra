package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.ClientContext;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Category;

public final class RulesContext {
	private static final String PARAM_CATEGORY = "category";
	private Context context;

	public RulesContext(Context context) {
		super();
		this.context = context;
	}

	public RulesContext(Context context, Category category) {
		super();
		this.context = context;
		setCategory(category.getName());
	}

	public void updateContext() {
		ClientContext.setContext(context);
	}

	public Context getContext() {
		return context;
	}

	public String getCategory() {
		return context.getParameter(PARAM_CATEGORY);
	}

	public RulesContext setCategory(String name) {
		return setParameter(PARAM_CATEGORY, name);
	}

	private RulesContext setParameter(String name, String value) {
		Map params = new HashMap();
		params.put(name, value);
		context = Context.create(context, params);
		return this;
	}
}
