package com.surelogic.sierra.gwt.client.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class Cache {
	private final List items = new ArrayList();
	private final List listeners = new ArrayList();

	public final void refresh() {
		doRefreshCall(new CacheCallback() {

			protected void callListener(CacheListener listener,
					Throwable failure) {
				listener.onRefresh(Cache.this, failure);
			}
		});
	}

	public final void save(final Cacheable item) {
		doSaveCall(new CacheCallback() {

			protected void callListener(CacheListener listener,
					Throwable failure) {
				listener.onItemUpdate(Cache.this, item, failure);
			}
		});
	}

	public final boolean isEmpty() {
		return items.isEmpty();
	}

	public final int getItemCount() {
		return items.size();
	}

	public final Cacheable getItem(int index) {
		return (Cacheable) items.get(index);
	}

	public final Cacheable getItem(String uuid) {
		for (Iterator it = items.iterator(); it.hasNext();) {
			final Cacheable nextItem = (Cacheable) it.next();
			if (LangUtil.equals(uuid, nextItem.getUuid())) {
				return nextItem;
			}
		}
		return null;
	}

	public final Iterator getItemIterator() {
		return items.iterator();
	}

	public final void addListener(CacheListener listener) {
		listeners.add(listener);
	}

	public final void removeListener(CacheListener listener) {
		listeners.remove(listener);
	}

	protected abstract void doRefreshCall(AsyncCallback callback);

	protected abstract void doSaveCall(AsyncCallback callback);

	private abstract class CacheCallback implements AsyncCallback {

		public CacheCallback() {
			super();
		}

		public void onFailure(Throwable caught) {
			ExceptionUtil.log(caught);
			for (Iterator it = listeners.iterator(); it.hasNext();) {
				callListener((CacheListener) it.next(), caught);
			}
		}

		public void onSuccess(Object result) {
			items.clear();
			items.addAll((List) result);
			for (Iterator it = listeners.iterator(); it.hasNext();) {
				callListener((CacheListener) it.next(), null);
			}
		}

		protected abstract void callListener(CacheListener listener,
				Throwable failure);

	}
}
