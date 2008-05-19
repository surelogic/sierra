package com.surelogic.sierra.gwt.client.data;

public class CacheListenerAdapter implements CacheListener {

	public void onItemUpdate(Cache cache, Cacheable item, Status status,
			Throwable failure) {
		// override this method if needed
	}

	public void onRefresh(Cache cache, Throwable failure) {
		// override this method if needed
	}

	public void onStartRefresh(Cache cache) {
		// override this method if needed
	}

}
