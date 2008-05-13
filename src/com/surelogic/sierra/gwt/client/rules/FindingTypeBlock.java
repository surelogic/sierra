package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;
import java.util.Set;

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

public class FindingTypeBlock extends SectionPanel {

	public void refresh(Category cat, boolean editing) {
		final VerticalPanel findingTypes = getContentPanel();
		findingTypes.clear();
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

	private Widget createDetailsRule(FilterEntry finding, boolean editing,
			boolean enabled) {
		if (editing) {
			final CheckBox rule = new CheckBox(finding.getName());
			rule.setTitle(finding.getShortMessage());
			rule.setChecked(enabled);
			return rule;
		}
		if (enabled) {
			final Label rule = new Label(finding.getName());
			rule.setTitle(finding.getShortMessage());
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

}
