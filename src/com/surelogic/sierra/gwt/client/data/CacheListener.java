package com.surelogic.sierra.gwt.client.data;

public interface CacheListener<E extends Cacheable> {

	void onStartRefresh(Cache<E> cache);

	void onRefresh(Cache<E> cache, Throwable failure);

	void onItemUpdate(Cache<E> cache, E item, Status status, Throwable failure);

}
