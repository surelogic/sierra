package com.surelogic.sierra.jdbc.tool;

public class ArtifactTypeDO {
	private long id;
	private String mnemonic;
	private String display;
	private String tool;
	private String version;

	ArtifactTypeDO(final long id, final String tool, final String mnemonic,
			final String display, final String version) {
		this.id = id;
		this.mnemonic = mnemonic;
		this.tool = tool;
		this.version = version;
		this.display = display;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(final String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(final String tool) {
		this.tool = tool;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(final String display) {
		this.display = display;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArtifactTypeDO other = (ArtifactTypeDO) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
