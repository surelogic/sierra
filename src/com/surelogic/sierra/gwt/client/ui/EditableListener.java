package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Widget;

public interface EditableListener<E> {

	void onEdit(Widget sender);

	void onCancelEdit(Widget sender);

	void onSave(Widget sender, E item);

}
