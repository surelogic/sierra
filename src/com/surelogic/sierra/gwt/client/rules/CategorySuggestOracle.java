package com.surelogic.sierra.gwt.client.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SettingsServiceAsync;

public class CategorySuggestOracle extends SuggestOracle {

	private final SettingsServiceAsync service;

	public CategorySuggestOracle() {
		service = ServiceHelper.getSettingsService();
	}

	public void requestSuggestions(final Request request,
			final Callback callback) {
		service.searchCategories(request.getQuery(), request.getLimit(),
				new AsyncCallback() {

					public void onFailure(Throwable caught) {
						// TODO
					}

					public void onSuccess(Object result) {
						final Map entries = (Map) result;
						final List suggestions = new ArrayList(entries.size());
						for (final Iterator i = entries.entrySet().iterator(); i
								.hasNext();) {
							final Entry e = (Entry) i.next();
							suggestions.add(new Suggestion((String) e.getKey(),
									(String) e.getValue()));
						}
						final Response response = new Response(suggestions);
						callback.onSuggestionsReady(request, response);
					}
				});
	}

	public static class Suggestion implements
			com.google.gwt.user.client.ui.SuggestOracle.Suggestion {

		private final String uuid;
		private final String display;

		Suggestion(String uuid, String display) {
			this.uuid = uuid;
			this.display = display;
		}

		public String getDisplayString() {
			return display;
		}

		public String getReplacementString() {
			return display;
		}

		public String getUuid() {
			return uuid;
		}

	}

}
