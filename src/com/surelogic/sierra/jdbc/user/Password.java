package com.surelogic.sierra.jdbc.user;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Password is a concrete class representing a user's password in the system.
 * 
 * @author nathan
 * 
 */
public final class Password {

	private static final String CHARSET = "UTF-8";
	private static final String ALGORITHM = "SHA-1";

	private final int salt;
	private final byte[] hash;

	private Password(int salt, byte[] hash) {
		this.salt = salt;
		this.hash = hash;
	}

	public int getSalt() {
		return salt;
	}

	public byte[] getHash() {
		return hash;
	}

	/**
	 * Check to see if the provided password matches this password.
	 * 
	 * @param password may not be null
	 * @return
	 */
	public boolean check(String password) {
		return MessageDigest.isEqual(hash, createHash(salt, password));
	}

	/**
	 * Restore a password from the specified salt and hash.
	 * 
	 * @param salt
	 * @param hash
	 * @return
	 */
	public static Password restorePassword(int salt, byte[] hash) {
		return new Password(salt, hash);
	}

	/**
	 * Create a new password from the specified password.
	 * 
	 * @param password may not be null
	 * @return
	 */
	public static Password newPassword(String password) {
		final int salt = newSalt();
		return new Password(salt, createHash(salt, password));
	}

	private static int newSalt() {
		final Random r = new Random(System.currentTimeMillis());
		return r.nextInt();
	}

	private static byte[] createHash(int salt, String password) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
			final byte[] saltedPassword = new byte[password.length() + 4];
			saltedPassword[0] = (byte) (salt >> 24);
			saltedPassword[1] = (byte) (salt >> 16);
			saltedPassword[2] = (byte) (salt >> 8);
			saltedPassword[3] = (byte) (salt >> 24);
			final byte[] passwordBytes = password.getBytes(CHARSET);
			System.arraycopy(passwordBytes, 0, saltedPassword, 4,
					passwordBytes.length);
			return digest.digest(passwordBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

}
