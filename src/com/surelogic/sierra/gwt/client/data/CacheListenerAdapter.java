package com.surelogic.sierra.gwt.client.data;

public class CacheListenerAdapter<E extends Cacheable> implements
		CacheListener<E> {

	public void onStartRefresh(Cache<E> cache) {
		// override this method if needed
	}

	public void onRefresh(Cache<E> cache, Throwable failure) {
		// override this method if needed
	}

	public void onItemUpdate(Cache<E> cache, E item, Status status,
			Throwable failure) {
		// override this method if needed
		cache.refresh();
	}

}
