package com.surelogic.sierra.tool.message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.jdbc.qualifier.QualifierManager;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class SierraServiceImpl extends SRPCServlet implements SierraService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8265889420077755990L;

	private static final Logger log = SLLogger
			.getLoggerFor(SierraServiceImpl.class);

	JAXBContext ctx;

	@PostConstruct
	public void init() {
		try {
			ctx = JAXBContext.newInstance(Settings.class);
		} catch (JAXBException e) {
			log.log(Level.SEVERE,
					"An error occurred while initializing a web service bean.",
					e);
		}
	}

	public SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException {
		SettingsReply reply = new SettingsReply();
		try {
			final ServerConnection server = ServerConnection.readOnly();
			try {
				final Connection conn = server.getConnection();
				try {
					checkServer(server, request.getServer());
					// return ServerSettingsManager.getInstance(conn)
					// .getLatestSettingsByProject(request.getProject(),
					// request.getRevision());
				} catch (Exception e) {
					exceptionNotify(
							server,
							"An error occurred attempting to retrieve the latest settings from the database.",
							e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return reply;
	}

	public void publishRun(Scan scan) {
		// TODO
	}

	@SuppressWarnings("unchecked")
	public Qualifiers getQualifiers(QualifierRequest request) {
		Qualifiers q = new Qualifiers();
		try {
			final ServerConnection server = ServerConnection.readOnly();
			try {
				final Connection conn = server.getConnection();
				try {
					q.setQualifier(QualifierManager.getInstance(conn)
							.getAllQualifierNames());
				} catch (SQLException e) {
					exceptionNotify(server, e.getMessage(), e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return q;
	}

	public CommitAuditTrailResponse commitAuditTrails(
			CommitAuditTrailRequest audits) throws ServerMismatchException {
		Connection conn;
		final CommitAuditTrailResponse response = new CommitAuditTrailResponse();
		final List<AuditTrail> trails = audits.getAuditTrail();
		if (trails != null && !trails.isEmpty()) {
			try {
				final ServerConnection server = ServerConnection.transaction();
				try {
					conn = server.getConnection();
					try {
						checkServer(server, audits.getServer());
						try {
							final long revision = server.nextRevision();
							response.setRevision(revision);
							response.setUid(ServerFindingManager.getInstance(
									conn).commitAuditTrails(
									User.getUser(getUserName(), conn).getId(),
									revision, trails));
							conn.commit();
						} catch (SQLException e) {
							conn.rollback();
							exceptionNotify(server, e.getMessage(), e);
						}
					} finally {
						try {
							conn.close();
						} catch (SQLException e) {
							exceptionNotify(server, e.getMessage(), e);
						}
					}
				} catch (SQLException e) {
					exceptionNotify(server, e.getMessage(), e);
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return response;
	}

	/**
	 * Finds or creates an trail that contains all of the provided matches for
	 * each requested merge. If none of the matches exist, we create a new trail
	 * and assign it to each match. If some of the matches belong to one trail,
	 * and the others none, then we merely assign the others to the existing
	 * trail. Finally, if we have multiple trails, we create an entirely new
	 * one, and obsolete each existing trail with the new one.
	 * 
	 * @throws ServerMismatchException
	 */
	public MergeAuditTrailResponse mergeAuditTrails(MergeAuditTrailRequest seed)
			throws ServerMismatchException {
		MergeAuditTrailResponse response = new MergeAuditTrailResponse();
		try {
			final ServerConnection server = ServerConnection.transaction();
			try {
				final Connection conn = server.getConnection();
				try {
					checkServer(server, seed.getServer());
					List<Merge> merges = seed.getMerge();
					final long revision = server.nextRevision();
					response.setTrail(ServerFindingManager.getInstance(conn)
							.mergeAuditTrails(seed.getProject(), revision,
									merges));
					conn.commit();
				} catch (SQLException e) {
					conn.rollback();
					exceptionNotify(server, e.getMessage(), e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return response;
	}

	/**
	 * @throws ServerMismatchException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public AuditTrailResponse getAuditTrails(GetAuditTrailRequest request)
			throws ServerMismatchException {
		AuditTrailResponse response = new AuditTrailResponse();
		try {
			final ServerConnection server = ServerConnection.readOnly();
			try {
				final Connection conn = server.getConnection();
				try {
					String project = request.getProject();
					Long revision = request.getRevision();
					checkServer(server, request.getServer());
					ServerFindingManager man = ServerFindingManager
							.getInstance(conn);
					response.setObsolete(man.getObsoletedTrails(project,
							revision));
					response.setUpdate(man.getAuditUpdates(project, revision));
				} catch (SQLException e) {
					exceptionNotify(server, e.getMessage(), e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return response;
	}

	public ServerUIDReply getUid(ServerUIDRequest request) {
		ServerUIDReply reply = new ServerUIDReply();
		try {
			final ServerConnection server = ServerConnection.readOnly();
			try {
				final Connection conn = server.getConnection();
				try {
					reply.setUid(server.getUid());
				} catch (SQLException e) {
					exceptionNotify(server, e.getMessage(), e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return reply;
	}

	private void checkServer(ServerConnection conn, String server)
			throws ServerMismatchException {
		String uid;
		try {
			uid = conn.getUid();
			if (!uid.equals(server)) {
				throw new ServerMismatchException(
						"The request's expected server did not match this server.");
			}
		} catch (SQLException e) {
			exceptionNotify(conn, e.getMessage(), e);
		}
	}

	private void exceptionNotify(ServerConnection server, String message,
			Throwable t) {
		log.log(Level.SEVERE, message, t);
		try {
			String email = server.getEmail();
			if (email != null) {
				Properties props = new Properties();
				props.put("mail.smtp.host", "zimbra.surelogic.com");
				props.put("mail.from", email);
				Session session = Session.getInstance(props, null);
				try {
					MimeMessage msg = new MimeMessage(session);
					msg.setFrom();
					msg.setRecipients(Message.RecipientType.TO, email);
					StringWriter s = new StringWriter();
					t.printStackTrace(new PrintWriter(s));
					msg.setSubject(getUserName() + " reports: " + message);
					msg.setSentDate(new Date());
					msg.setText(s.toString());
					Transport.send(msg);
				} catch (MessagingException mex) {
					log.log(Level.SEVERE,
							"Mail notification of exception failed.", mex);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	public GlobalSettings getGlobalSettings(GlobalSettingsRequest request) {
		try {
			final ServerConnection server = ServerConnection.readOnly();
			try {
				final Connection conn = server.getConnection();
				try {
					final GlobalSettings settings = new GlobalSettings();
					settings.setFilter(SettingsManager.getInstance(conn)
							.getGlobalSettings());
					return settings;
				} catch (SQLException e) {
					exceptionNotify(server, e.getMessage(), e);
				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return new GlobalSettings();
	}

	public void writeGlobalSettings(GlobalSettings settings) {
		try {
			final ServerConnection server = ServerConnection.transaction();
			try {
				final Connection conn = server.getConnection();
				try {
					SettingsManager.getInstance(conn).writeGlobalSettings(
							settings.getFilter());
					conn.commit();
				} catch (SQLException e) {
					conn.rollback();
					throw e;
				} finally {
					try {
						conn.close();
					} catch (SQLException e) {
						exceptionNotify(server, e.getMessage(), e);
					}
				}
			} catch (SQLException e) {
				exceptionNotify(server, e.getMessage(), e);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}
