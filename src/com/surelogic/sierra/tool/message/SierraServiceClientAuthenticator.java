package com.surelogic.sierra.tool.message;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SierraServiceClientAuthenticator extends Authenticator {

	private final String username;
	private final String password;

	public SierraServiceClientAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return (new PasswordAuthentication(this.username,
				this.password.toCharArray()));
	}
}