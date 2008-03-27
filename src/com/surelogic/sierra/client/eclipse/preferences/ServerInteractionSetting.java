package com.surelogic.sierra.client.eclipse.preferences;

public enum ServerInteractionSetting {
	NEVER {
		@Override
		public String getLabel() {
			return "Never";
		}
	},
	CHECK {
		@Override
		public String getLabel() {
			return "Check for audit updates, but use manual synchronization";
		}
	},
	PERIODIC {
		@Override
		public String getLabel() {
			return "Automatically synchronize audits periodically";
		}
	},
	THRESHOLD {
		@Override
		public String getLabel() {
			return "Automatically synchronize audits when threshold is reached";
		}
	};
	abstract public String getLabel();
}
