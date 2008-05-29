package com.surelogic.sierra.gwt.client.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class Cache<E extends Cacheable> {
	private final List<E> items = new ArrayList<E>();
	private final List<CacheListener<E>> listeners = new ArrayList<CacheListener<E>>();

	public final void refresh() {
		for (final CacheListener<E> listener : listeners) {
			listener.onStartRefresh(this);
		}
		doRefreshCall(new CacheCallback<List<E>>() {

			@Override
			protected void processResult(List<E> result) {
				items.clear();
				items.addAll(result);
			}

			@Override
			protected void callListener(CacheListener<E> listener,
					List<E> result, Throwable failure) {
				listener.onRefresh(Cache.this, failure);
			}

		});
	}

	public final void save(final E item) {
		doSaveCall(item, new CacheCallback<Status>() {

			@Override
			protected void processResult(Status result) {
				if (result.isSuccess()) {
					final int itemIndex = getItemIndex(item);
					if (itemIndex != -1) {
						items.remove(itemIndex);
						items.add(itemIndex, item);
					} else {
						items.add(item);
					}

				}
			}

			@Override
			protected void callListener(CacheListener<E> listener,
					Status result, Throwable failure) {
				listener.onItemUpdate(Cache.this, item, result, failure);
			}
		});
	}

	public final boolean isEmpty() {
		return items.isEmpty();
	}

	public final int getItemCount() {
		return items.size();
	}

	public final E getItem(int index) {
		return items.get(index);
	}

	public final E getItem(String uuid) {
		for (final E item : items) {
			if (LangUtil.equals(uuid, item.getUuid())) {
				return item;
			}
		}
		return null;
	}

	public final int getItemIndex(E item) {
		return getItemIndex(item.getUuid());
	}

	public final int getItemIndex(String uuid) {
		for (int i = 0; i < items.size(); i++) {
			if (LangUtil.equals(uuid, items.get(i).getUuid())) {
				return i;
			}
		}
		return -1;
	}

	public final List<E> getItems() {
		return Collections.unmodifiableList(items);
	}

	public final Iterator<E> getItemIterator() {
		return items.iterator();
	}

	public final void addListener(CacheListener<E> listener) {
		listeners.add(listener);
	}

	public final void removeListener(CacheListener<E> listener) {
		listeners.remove(listener);
	}

	protected abstract void doRefreshCall(AsyncCallback<List<E>> callback);

	protected abstract void doSaveCall(Cacheable item,
			AsyncCallback<Status> callback);

	private abstract class CacheCallback<T> implements AsyncCallback<T> {

		public void onFailure(Throwable caught) {
			ExceptionUtil.log(caught);
			for (final CacheListener<E> listener : listeners) {
				callListener(listener, null, caught);
			}
		}

		public void onSuccess(T result) {
			processResult(result);
			for (final CacheListener<E> listener : listeners) {
				callListener(listener, result, null);
			}
		}

		protected abstract void processResult(T result);

		protected abstract void callListener(CacheListener<E> listener,
				T result, Throwable failure);

	}

}
