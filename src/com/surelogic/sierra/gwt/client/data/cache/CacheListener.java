package com.surelogic.sierra.gwt.client.data.cache;

import com.surelogic.sierra.gwt.client.data.Status;

public interface CacheListener<E extends Cacheable> {

	void onStartRefresh(Cache<E> cache);

	void onRefresh(Cache<E> cache, Throwable failure);

	void onItemUpdate(Cache<E> cache, E item, Status status, Throwable failure);

}
