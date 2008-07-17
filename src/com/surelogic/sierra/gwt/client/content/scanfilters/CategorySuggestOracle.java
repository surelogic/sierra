package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
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
				new AsyncCallback<Map<String, String>>() {

					public void onFailure(Throwable caught) {
						// TODO
					}

					public void onSuccess(Map<String, String> result) {
						final List<Suggestion> suggestions = new ArrayList<Suggestion>(
								result.size());
						for (final Entry<String, String> e : result.entrySet()) {
							suggestions.add(new Suggestion(e.getKey(), e
									.getValue()));
						}
						final Response response = new Response(suggestions);
						callback.onSuggestionsReady(request, response);
					}
				});
	}

	public static class Suggestion implements
			com.google.gwt.user.client.ui.SuggestOracle.Suggestion,
			Comparable<Suggestion> {

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

		public ScanFilterEntry getEntry() {
			final ScanFilterEntry e = new ScanFilterEntry();
			e.setUuid(uuid);
			e.setName(display);
			e.setCategory(true);
			return e;
		}

		public int compareTo(Suggestion that) {
			return display.compareTo((that).display);
		}
	}

}
