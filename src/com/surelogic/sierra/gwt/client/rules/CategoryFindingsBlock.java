package com.surelogic.sierra.gwt.client.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	private static final String PRIMARY_STYLE = CategoryBlock.PRIMARY_STYLE;
	private final Map findings = new HashMap();
	private boolean editing;

	public void refresh(Category cat, boolean editing) {
		this.editing = editing;

		final VerticalPanel findingTypes = getContentPanel();
		findingTypes.clear();
		findings.clear();

		final List visibleFindings = new ArrayList();
		for (final Iterator it = cat.getEntries().iterator(); it.hasNext();) {
			final FilterEntry finding = (FilterEntry) it.next();
			final String findingUuid = finding.getUuid();
			if (visibleFindings.indexOf(findingUuid) == -1) {
				final Widget findingUI = createDetailsRule(finding, editing,
						!finding.isFiltered());
				if (findingUI != null) {
					findingTypes.add(findingUI);
					visibleFindings.add(findingUuid);
				}
			}
		}
		final Set excluded = cat.getExcludedEntries();
		for (final Iterator catIt = cat.getParents().iterator(); catIt
				.hasNext();) {
			final Category parent = (Category) catIt.next();
			VerticalPanel targetPanel;
			if (editing) {
				final DisclosurePanel parentPanel = new DisclosurePanel(
						"From: " + parent.getName());
				findingTypes.add(parentPanel);
				targetPanel = new VerticalPanel();
				parentPanel.setContent(targetPanel);
				parentPanel.setOpen(true);
			} else {
				targetPanel = findingTypes;
			}

			final Set parentFindings = parent.getIncludedEntries();
			for (final Iterator findingIt = parentFindings.iterator(); findingIt
					.hasNext();) {
				final FilterEntry finding = (FilterEntry) findingIt.next();
				final String findingUuid = finding.getUuid();
				if (visibleFindings.indexOf(findingUuid) == -1) {
					final Widget findingUI = createDetailsRule(finding,
							editing, !excluded.contains(finding));
					if (findingUI != null) {
						targetPanel.add(findingUI);
						visibleFindings.add(findingUuid);
					}
				}
			}

		}

		if (editing) {
			addAction("Add Category", new ClickListener() {

				public void onClick(Widget sender) {
					addCategory();
				}
			});
		} else {
			removeActions();
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
			rule.addStyleName(PRIMARY_STYLE + "-finding");
			findings.put(rule, finding);
			return rule;
		}
		if (enabled) {
			final Label rule = new Label(finding.getName());
			rule.setTitle(finding.getShortMessage());
			rule.addStyleName("clickable2");
			rule.addClickListener(new FindingTypeListener(finding));
			rule.addStyleName(PRIMARY_STYLE + "-finding");
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
			// TODO disabled while porting Categories and Findings - new
			// CategoriesContext(finding).updateContext();
		}

	}

}
