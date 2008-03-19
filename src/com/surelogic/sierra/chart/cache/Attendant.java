package com.surelogic.sierra.chart.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.http.HttpSession;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This singleton class manages tickets used for chart caching within and across
 * sessions. It tries to share tickets across sessions safely by using a node
 * ticket list.
 * <p>
 * This class is thread safe.
 * <p>
 * <i>Implementation note:</i> Any access to the node or session ticket sets
 * requires a lock on {@code this}.
 */
public final class Attendant {

	/**
	 * The set of tickets cached on this node. This is intended to allow sharing
	 * between sessions as best we can.
	 * <p>
	 * TODO: This class grows forever, this is bad and needs to be fixed.
	 */
	public final Set<Ticket> f_nodeTickets = new HashSet<Ticket>();

	/**
	 * Gets or creates a {@link Ticket} object for the passed set of parameters.
	 * This ticket object may be used throughout the lifetime of the passed
	 * session.
	 * 
	 * @param parameters
	 *            the set of parameters to provide a ticket for.
	 * @param session
	 *            the session the {@link Ticket} object is associated with.
	 * @return the {@link Ticket} object.
	 */
	public Ticket getTicket(final Map<String, String> parameters,
			final HttpSession session) {
		if (parameters == null)
			throw new IllegalArgumentException(I18N.err(44, "parameters"));
		if (session == null)
			throw new IllegalArgumentException(I18N.err(44, "session"));

		synchronized (this) {
			Set<Ticket> tickets = getOrCreateSessionTickets(session);
			/*
			 * This set of tickets could have just migrated over from another
			 * machine in a cluster. So we need to add all these tickets to this
			 * node.
			 */
			f_nodeTickets.addAll(tickets);
			/*
			 * Do we have a ticket for this set of parameters?
			 */
			for (Ticket ticket : tickets) {
				if (ticket.getParameters().equals(parameters)) {
					SLLogger.log(Level.FINE,
							"getTicket found the ticket in the session.");
					/*
					 * Yes, return the ticket.
					 */
					return ticket;
				}
			}
			/*
			 * No, check this node.
			 */
			for (Ticket ticket : f_nodeTickets) {
				if (ticket.getParameters().equals(parameters)) {
					SLLogger.log(Level.FINE,
							"getTicket found the ticket in this node.");
					/*
					 * Yes, add the ticket to the session and return the ticket.
					 */
					tickets.add(ticket);
					return ticket;
				}
			}
			/*
			 * Double No, create the ticket.
			 */
			SLLogger.log(Level.FINE, "getTicket created the ticket.");
			final Ticket ticket = new Ticket(parameters);
			tickets.add(ticket);
			f_nodeTickets.add(ticket);
			return ticket;
		}
	}

	/**
	 * Gets the {@link Ticket} object associated with the passed session with
	 * the given universally unique identifier (UUID).
	 * 
	 * @param uuid
	 *            the universally unique identifier (UUID) of the desired
	 *            ticket.
	 * @param session
	 *            the session the {@link Ticket} object is associated with.
	 * @return the {@link Ticket} object, or {@code null} if none.
	 */
	public Ticket getTicket(final UUID uuid, final HttpSession session) {
		if (uuid == null)
			throw new IllegalArgumentException(I18N.err(44, "uuid"));
		if (session == null)
			throw new IllegalArgumentException(I18N.err(44, "session"));

		synchronized (this) {
			Set<Ticket> tickets = getOrCreateSessionTickets(session);
			for (Ticket ticket : tickets) {
				if (ticket.getUUID().equals(uuid)) {
					return ticket;
				}
			}
		}
		return null;
	}

	/**
	 * Gets or creates the set of tickets associated with the passed session.
	 * <p>
	 * Callers must be holding a lock on {@code this}.
	 * 
	 * @param session
	 *            the session to get or create the set of tickets for.
	 * @return a set of tickets (never {@code null}).
	 */
	@SuppressWarnings("unchecked")
	private Set<Ticket> getOrCreateSessionTickets(final HttpSession session) {

		final String TICKET_ID = "com.surelogic.chart.tickets";

		Set<Ticket> result = (Set<Ticket>) session.getAttribute(TICKET_ID);
		if (result == null) {
			result = new HashSet<Ticket>();
			session.setAttribute(TICKET_ID, result);
		}
		return result;
	}

	/**
	 * The singleton instance.
	 */
	private static final Attendant INSTANCE = new Attendant();

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static Attendant getInstance() {
		return INSTANCE;
	}

	private Attendant() {
		// singleton
	}
}
