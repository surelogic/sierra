package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.UserAccount;

public class LoginResult implements Serializable {
	private static final long serialVersionUID = -5472919019159995448L;

	private String errorMessage;
	private UserAccount userAccount;

	public LoginResult() {
		super();
	}

	public LoginResult(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	public LoginResult(UserAccount userAccount) {
		super();
		this.userAccount = userAccount;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
}
