package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Widget;

public class EditableListenerAdapter<E> implements EditableListener<E> {

	public void onCancelEdit(Widget sender) {
		// override this method if needed
	}

	public void onEdit(Widget sender) {
		// override this method if needed
	}

	public void onSave(Widget sender, E item) {
		// override this method if needed
	}

}
