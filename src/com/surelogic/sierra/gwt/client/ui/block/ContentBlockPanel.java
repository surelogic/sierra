package com.surelogic.sierra.gwt.client.ui.block;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlock.ContentBlockListener;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class ContentBlockPanel extends BlockPanel implements
		ContentBlockListener {
	private final ContentBlock<?> block;

	public ContentBlockPanel(final ContentBlock<?> block) {
		super();
		this.block = block;
	}

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(block);
		contentPanel.setCellHorizontalAlignment(block, block
				.getHorizontalAlignment());
		onRefresh(block);
	}

	public final ContentBlock<?> getBlock() {
		return block;
	}

	public void onRefresh(final ContentBlock<?> sender) {
		setTitle(sender.getName());
		setSummary(sender.getSummary());
		setStatus(sender.getStatus());
		removeActions();
		for (final Widget action : sender.getActions()) {
			addAction(action);
		}
	}

}
