package com.surelogic.sierra.gwt.client.content.servermgmt;

import com.google.gwt.user.client.ui.DockPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.table.TableSection;

public class ServerManagementContent extends ContentComposite {

	private static final ServerManagementContent instance = new ServerManagementContent();

	private final ServerList serverList = new ServerList();

	@Override
	protected void onDeactivate() {

	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {

	}

	@Override
	protected void onUpdate(Context context) {

	}

	private void refreshServerList() {

	}

	private class ServerList extends TableSection {

		@Override
		protected ColumnData[] getHeaderDataTypes() {
			return null;
		}

		@Override
		protected String[] getHeaderTitles() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void updateTable(Context context) {
			// TODO Auto-generated method stub

		}

	}

	public static ServerManagementContent getInstance() {
		return instance;
	}

}
