package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class CategoryFindingsBlock extends SectionPanel {
	private final Map findings = new HashMap();
	private boolean editing;

	public void refresh(Category cat, boolean editing) {
		this.editing = editing;

		final VerticalPanel findingTypes = getContentPanel();
		findingTypes.clear();
		findings.clear();

		if (editing) {
			addAction("Add Category", new ClickListener() {

				public void onClick(Widget sender) {
					addCategory();
				}
			});
		} else {
			removeActions();
		}

		for (final Iterator it = cat.getEntries().iterator(); it.hasNext();) {
			final FilterEntry finding = (FilterEntry) it.next();
			final Widget findingUI = createDetailsRule(finding, editing,
					!finding.isFiltered());
			if (findingUI != null) {
				findingTypes.add(findingUI);
			}
		}
		final Set excluded = cat.getExcludedEntries();
		for (final Iterator catIt = cat.getParents().iterator(); catIt
				.hasNext();) {
			final Category parent = (Category) catIt.next();
			final DisclosurePanel parentPanel = new DisclosurePanel("From: "
					+ parent.getName());
			final VerticalPanel parentFindingsPanel = new VerticalPanel();
			final Set parentFindings = parent.getIncludedEntries();
			for (final Iterator findingIt = parentFindings.iterator(); findingIt
					.hasNext();) {
				final FilterEntry finding = (FilterEntry) findingIt.next();
				final Widget findingUI = createDetailsRule(finding, editing,
						!excluded.contains(finding));
				if (findingUI != null) {
					parentFindingsPanel.add(findingUI);
				}
			}
			parentPanel.setContent(parentFindingsPanel);
			parentPanel.setOpen(true);
			findingTypes.add(parentPanel);
		}
	}

	public void saveTo(Category cat) {
		if (!editing) {
			return;
		}

		for (Iterator it = findings.entrySet().iterator(); it.hasNext();) {
			Entry findingEntry = (Entry) it.next();
			CheckBox ui = (CheckBox) findingEntry.getKey();
			FilterEntry finding = (FilterEntry) findingEntry.getValue();
			updateFilterEntry(cat.getEntries(), finding, !ui.isChecked());
		}
	}

	private void updateFilterEntry(Set entries, FilterEntry finding,
			boolean filtered) {
		final String findingUuid = finding.getUuid();
		for (Iterator it = entries.iterator(); it.hasNext();) {
			FilterEntry nextFilter = (FilterEntry) it.next();
			if (findingUuid.equals(nextFilter.getUuid())) {
				nextFilter.setFiltered(filtered);
				break;
			}
		}
	}

	private Widget createDetailsRule(FilterEntry finding, boolean editing,
			boolean enabled) {
		if (editing) {
			final CheckBox rule = new CheckBox(finding.getName());
			rule.setTitle(finding.getShortMessage());
			rule.setChecked(enabled);
			findings.put(rule, finding);
			return rule;
		}
		if (enabled) {
			final Label rule = new Label(finding.getName());
			rule.setTitle(finding.getShortMessage());
			rule.addStyleName("clickable2");
			rule.addClickListener(new FindingTypeListener(finding));
			findings.put(rule, finding);
			return rule;
		}
		return null;
	}

	private void addCategory() {
		// TODO need a dialog or UI update to add categories + findings
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Finding Type");
	}

	protected void onActivate(Context context) {
		// nothing to do
	}

	protected void onUpdate(Context context) {
		// nothing to do
	}

	protected void onDeactivate() {
		// nothing to do
	}

	private class FindingTypeListener implements ClickListener {
		private final FilterEntry finding;

		public FindingTypeListener(FilterEntry finding) {
			super();
			this.finding = finding;
		}

		public void onClick(Widget sender) {
			new RulesContext(finding).updateContext();
		}

	}

}
