package com.surelogic.sierra.jdbc.tool;

public class ArtifactTypeDO {
	long id;
	String mnemonic;
	String tool;

	ArtifactTypeDO(final long id, final String tool, final String mnemonic) {
		this.id = id;
		this.mnemonic = mnemonic;
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
