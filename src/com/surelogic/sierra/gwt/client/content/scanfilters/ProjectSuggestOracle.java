package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ProjectSuggestOracle extends SuggestOracle {

	@Override
	public void requestSuggestions(final Request request,
			final Callback callback) {
		ServiceHelper.getSettingsService().searchProjects(request.getQuery(),
				request.getLimit(), new AsyncCallback<List<String>>() {
					public void onFailure(Throwable caught) {
						// TODO
					}

					public void onSuccess(List<String> result) {
						final List<Suggestion> suggestions = new ArrayList<Suggestion>(
								result.size());
						for (final String project : result) {
							suggestions.add(new Suggestion(project));
						}
						final Response response = new Response(suggestions);
						callback.onSuggestionsReady(request, response);
					}
				});
	}

	private static class Suggestion implements
			com.google.gwt.user.client.ui.SuggestOracle.Suggestion {

		private final String project;

		Suggestion(String project) {
			this.project = project;
		}

		public String getDisplayString() {
			return project;
		}

		public String getReplacementString() {
			return project;
		}

	}

}
