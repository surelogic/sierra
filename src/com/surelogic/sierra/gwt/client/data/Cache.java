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
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			((CacheListener) it.next()).onStartRefresh(this);
		}
		doRefreshCall(new CacheCallback() {

			protected void processResult(Object result) {
				items.clear();
				items.addAll((List) result);
			}

			protected void callListener(CacheListener listener, Object result,
					Throwable failure) {
				listener.onRefresh(Cache.this, failure);
			}
		});
	}

	public final void save(final Cacheable item) {
		doSaveCall(item, new CacheCallback() {

			protected void processResult(Object result) {
				Status s = (Status) result;
				if (s.isSuccess()) {
					int itemIndex = getItemIndex(item);
					if (itemIndex != -1) {
						items.remove(itemIndex);
						items.add(itemIndex, item);
					} else {
						items.add(item);
					}

				}
			}

			protected void callListener(CacheListener listener, Object result,
					Throwable failure) {
				listener.onItemUpdate(Cache.this, item, (Status) result,
						failure);
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

	public final int getItemIndex(Cacheable item) {
		final String uuid = item.getUuid();
		for (int i = 0; i < items.size(); i++) {
			if (LangUtil.equals(uuid, ((Cacheable) items.get(i)).getUuid())) {
				return i;
			}
		}
		return -1;
	}

	public final int getItemIndex(String uuid) {
		for (int i = 0; i < items.size(); i++) {
			if (LangUtil.equals(uuid, ((Cacheable) items.get(i)).getUuid())) {
				return i;
			}
		}
		return -1;
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

	protected abstract void doSaveCall(Cacheable item, AsyncCallback callback);

	private abstract class CacheCallback implements AsyncCallback {

		public void onFailure(Throwable caught) {
			ExceptionUtil.log(caught);
			for (Iterator it = listeners.iterator(); it.hasNext();) {
				callListener((CacheListener) it.next(), null, caught);
			}
		}

		public void onSuccess(Object result) {
			processResult(result);
			for (Iterator it = listeners.iterator(); it.hasNext();) {
				callListener((CacheListener) it.next(), result, null);
			}
		}

		protected abstract void processResult(Object result);

		protected abstract void callListener(CacheListener listener,
				Object result, Throwable failure);

	}

}
