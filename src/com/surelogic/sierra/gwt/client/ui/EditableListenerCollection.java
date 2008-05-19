package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.ui.Widget;

public class EditableListenerCollection {
	private final java.util.List listeners = new ArrayList();

	public void addListener(EditableListener listener) {
		if (listeners.indexOf(listener) == -1) {
			listeners.add(listener);
		}
	}

	public void removeListener(EditableListener listener) {
		listeners.remove(listener);
	}

	public void fireEdit(Widget sender) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			((EditableListener) it.next()).onEdit(sender);
		}
	}

	public void fireCancelEdit(Widget sender) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			((EditableListener) it.next()).onCancelEdit(sender);
		}
	}

	public void fireSave(Widget sender, Object item) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			((EditableListener) it.next()).onSave(sender, item);
		}
	}

}
