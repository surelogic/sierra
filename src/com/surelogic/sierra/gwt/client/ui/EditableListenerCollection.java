package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public class EditableListenerCollection<E> {
	private final List<EditableListener<E>> listeners = new ArrayList<EditableListener<E>>();

	public void addListener(EditableListener<E> listener) {
		if (listeners.indexOf(listener) == -1) {
			listeners.add(listener);
		}
	}

	public void removeListener(EditableListener<E> listener) {
		listeners.remove(listener);
	}

	public void fireEdit(Widget sender) {
		for (EditableListener<E> listener : listeners) {
			listener.onEdit(sender);
		}
	}

	public void fireCancelEdit(Widget sender) {
		for (EditableListener<E> listener : listeners) {
			listener.onCancelEdit(sender);
		}
	}

	public void fireSave(Widget sender, E item) {
		for (EditableListener<E> listener : listeners) {
			listener.onSave(sender, item);
		}
	}

}
