package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.sierra.client.eclipse.model.*;

public abstract class AbstractSierraViewMediator 
extends AbstractDatabaseObserver implements IViewMediator 
{
	public static final Listener DO_NOTHING = new Listener() {
		public void handleEvent(Event event) {
			// Do nothing
		}
	};
	
	protected final IViewCallback f_view;
	
	protected AbstractSierraViewMediator(IViewCallback cb) {
		f_view = cb;
	}
	
	public Listener getNoDataListener() {
		return DO_NOTHING;
	}
	
	public void init() {
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
	}
}
