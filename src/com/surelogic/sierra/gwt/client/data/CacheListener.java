package com.surelogic.sierra.gwt.client.data;

public interface CacheListener {

	void onRefresh(Cache cache, Throwable failure);

	void onItemUpdate(Cache cache, Cacheable item, Throwable failure);

}
