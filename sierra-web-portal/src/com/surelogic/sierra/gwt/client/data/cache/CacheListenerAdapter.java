package com.surelogic.sierra.gwt.client.data.cache;

import com.surelogic.sierra.gwt.client.data.Status;

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
