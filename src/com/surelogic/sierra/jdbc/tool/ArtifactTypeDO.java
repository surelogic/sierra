package com.surelogic.sierra.jdbc.tool;

public class ArtifactTypeDO {
	long id;
	String mnemonic;

	ArtifactTypeDO(long id, String mnemonic) {
		this.id = id;
		this.mnemonic = mnemonic;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
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
