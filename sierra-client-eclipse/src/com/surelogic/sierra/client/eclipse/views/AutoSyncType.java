package com.surelogic.sierra.client.eclipse.views;

enum AutoSyncType {
	ON, OFF, MIXED() {
		@Override
		boolean areAllSame() {
			return false;
		}
	};

	boolean areAllSame() {
		return true;
	}
	
	AutoSyncType combine(AutoSyncType other) {
		if (other == this) {
			return this;
		}
		return MIXED;
	}
}
