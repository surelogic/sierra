package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Widget;

public interface SelectableGridListener {

	void onHeaderClick(Widget source, int column);

	void onClick(Widget source, int row, int column, Object rowData);

	boolean onChange(Widget source, int row, int column, Object oldValue,
			Object newValue);

}
