package com.surelogic.sierra.tool.message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class SupportServiceImpl extends SRPCServlet implements SupportService {
	private static final long serialVersionUID = 4144761373500031238L;
	static {
		SLLogger.getLogger().warning("Starting SupportService");
	}

	public SupportReply request(final SupportRequest r, final File... files) {
		SLLogger.getLogger().warning("SupportService.request()");
		if (r.getType() == null) {
			return new SupportReply("Null type");
		}
		switch (r.getType()) {
		case REGISTER:
		case UPDATE:
		case USAGE:
		case ERROR:
			final File props = writeProps(r.getPairs());
			notifyAdmin("Test", "Testing attachments", prepend(props, files));
			break;
		default:
			return new SupportReply("Unknown request type");
		}
		return new SupportReply("OK");
	}

	private File writeProps(final Map<String, String> pairs) {
		PrintWriter pw = null;
		try {
			final File f = File.createTempFile("Test", ".tmp");
			final FileWriter fw = new FileWriter(f);
			pw = new PrintWriter(fw);
			for (final Map.Entry<String, String> e : pairs.entrySet()) {
				pw.println(e.getKey() + "=" + e.getValue());
			}
			pw.close();
			return f;
		} catch (final IOException e) {
			if (pw != null) {
				e.printStackTrace(pw);
			} else {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static File[] prepend(final File f, final File[] files) {
		if (f == null || !f.exists()) {
			return files;
		}
		final File[] newFiles = new File[files.length + 1];
		if (files.length > 0) {
			System.arraycopy(files, 0, newFiles, 1, files.length);
		}
		files[0] = f;
		return files;
	}

	private void notifyAdmin(final String subject, final String message,
			final File... files) {
		final String host = "smtp.gmail.com";
		final String port = "587";
		final String user = "changedIn99@gmail.com";
		final String pass = "nocertainty";
		final String from = "changedIn99@gmail.com";
		final String to = "edwin.chan@surelogic.com";
		final Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		if (port != null) {
			props.put("mail.smtp.port", port);
		}
		Authenticator auth;
		if ((user != null) && (user.length() > 0)) {
			auth = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, pass);
				}
			};
		} else {
			auth = null;
		}
		final Session session = Session.getInstance(props, auth);
		try {
			final MimeMessage msg = new MimeMessage(session);
			msg.setSender(new InternetAddress(
					((from == null) || (from.length() == 0)) ? to : from));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			msg.setSubject(subject);
			msg.setSentDate(new Date());

			final Multipart multipart = new MimeMultipart();
			setupMessage(multipart, message);

			for (final File f : files) {
				addAttachment(multipart, f);
			}

			// Put parts in message
			msg.setContent(multipart);
			Transport.send(msg);
		} catch (final MessagingException mex) {
			log
					.log(Level.SEVERE,
							"Mail notification of exception failed.", mex);
		}
	}

	private static void setupMessage(final Multipart multipart,
			final String message) throws MessagingException {
		// Create the message part
		final BodyPart messageBodyPart = new MimeBodyPart();

		// Fill the message
		messageBodyPart.setText(message);
		// msg.setContent(message, "text/plain");

		multipart.addBodyPart(messageBodyPart);
	}

	private static void addAttachment(final Multipart multipart, final File file)
			throws MessagingException {
		final BodyPart messageBodyPart = new MimeBodyPart();

		// Get the attachment
		final DataSource source = new FileDataSource(file);

		// Set the data handler to the attachment
		messageBodyPart.setDataHandler(new DataHandler(source));

		// Set the filename
		// FIX modify to eliminate temp name?
		messageBodyPart.setFileName(file.getName());

		multipart.addBodyPart(messageBodyPart);
	}
}
