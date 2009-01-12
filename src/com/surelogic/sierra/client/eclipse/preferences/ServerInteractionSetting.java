package com.surelogic.sierra.client.eclipse.preferences;

public enum ServerInteractionSetting {
	NEVER {
		@Override
		public String getLabel() {
			return "Never";
		}
	},
	/*
	CHECK {
		@Override
		public String getLabel() {
			return "Check for audit updates, but use manual synchronization";
		}
		@Override
		public boolean doServerAutoUpdate() {
			return true;
		}
	},
	*/
	PERIODIC {
		@Override
		public String getLabel() {
			return "Automatically synchronize audits (periodically)";
		}
		@Override
		public boolean doServerAutoSync() {
			return true;
		}
	},
	THRESHOLD {
		@Override
		public String getLabel() {
			return "Automatically synchronize audits when audit threshold is reached";
		}
		@Override
		public boolean useAuditThreshold() {
			return true;
		}
	};
	abstract public String getLabel();
	public boolean doServerAutoUpdate() {
		return false;
	}
	public boolean doServerAutoSync() {
		return false;
	}
	public boolean useAuditThreshold() {
		return false;
	}
}
