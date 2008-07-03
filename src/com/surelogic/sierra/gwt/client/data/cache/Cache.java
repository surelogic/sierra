package com.surelogic.sierra.gwt.client.data.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class Cache<E extends Cacheable> implements Iterable<E> {
	public static final long REFRESH_DELAY = 30 * 1000;

	private final List<E> items = new ArrayList<E>();
	private final Set<CacheListener<E>> listeners = new HashSet<CacheListener<E>>();
	private long lastRefresh;

	public final void refresh() {
		refresh(true);
	}

	public final void refresh(boolean force) {
		final long currentTime = new Date().getTime();
		if (!force && (lastRefresh + REFRESH_DELAY <= currentTime)) {
			return;
		}

		lastRefresh = currentTime;

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

	/**
	 * Clears the cache without triggering refresh events.
	 * 
	 */
	public final void clear() {
		items.clear();
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

	public final Iterator<E> iterator() {
		return items.iterator();
	}

	public final void addListener(CacheListener<E> listener) {
		listeners.add(listener);
	}

	public final void removeListener(CacheListener<E> listener) {
		listeners.remove(listener);
	}

	protected abstract void doRefreshCall(AsyncCallback<List<E>> callback);

	protected abstract void doSaveCall(E item, AsyncCallback<Status> callback);

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
