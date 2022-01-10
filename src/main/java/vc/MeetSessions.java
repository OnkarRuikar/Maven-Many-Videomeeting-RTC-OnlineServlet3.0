package vc;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import vc.cmd.Answer;
import vc.cmd.HeartBeat;
import vc.cmd.OfferSDP;
import vc.cmd.RequestSession;

public class MeetSessions {

	private Set<MeetSession> sessions = new LinkedHashSet<>();
	private List<Handler> registry = Arrays.asList(new RequestSession(this), new Answer(this), new HeartBeat(this),
			new OfferSDP(this));

	private Set<MeetConnection> offererAnswererConnection = new LinkedHashSet<>();

	private Set<BiConsumer<MeetSession, Set<MeetSession>>> onIncommingBrowserTabHandlers = new LinkedHashSet<>();
	private Set<BiConsumer<MeetSession, Set<MeetSession>>> onOutgoingBrowserTabHandlers = new LinkedHashSet<>();

	public MeetSession registerNewInstance() {
		MeetSession e;
		synchronized (sessions) {
			Set<UUID> usedIds = new LinkedHashSet<UUID>();
			for (MeetSession meetSession : sessions) {
				usedIds.add(meetSession.getSessionId());
			}
			UUID newRandomUUID = UUID.randomUUID();
			while (usedIds.contains(newRandomUUID)) {
				newRandomUUID = UUID.randomUUID();
			}
			e = new MeetSession(newRandomUUID);
			sessions.add(e);
		}
		for (BiConsumer<MeetSession, Set<MeetSession>> onIncommingBrowserTabHandler : onIncommingBrowserTabHandlers) {
			onIncommingBrowserTabHandler.accept(e, sessions);
		}
		return e;
	}

	public MeetSession getById(String requestedSessionId) {
		UUID requestedSession = UUID.fromString(requestedSessionId);
		synchronized (sessions) {
			for (MeetSession session : sessions) {
				if (session.getSessionId().equals(requestedSession)) {
					return session;
				}
			}
		}
		return null;
	}

	public void addOnIncommingBrowserTabHandler(BiConsumer<MeetSession, Set<MeetSession>> handler) {
		onIncommingBrowserTabHandlers.add(handler);
	}

	public boolean removeOnIncommingBrowserTabHandler(BiConsumer<MeetSession, Set<MeetSession>> handler) {
		return onIncommingBrowserTabHandlers.remove(handler);
	}

	public void addTooOldBrowserTabHandler(BiConsumer<MeetSession, Set<MeetSession>> handler) {
		onOutgoingBrowserTabHandlers.add(handler);
	}

	public boolean removeTooOldBrowserTabHandler(BiConsumer<MeetSession, Set<MeetSession>> handler) {
		return onOutgoingBrowserTabHandlers.remove(handler);
	}

	public void removeSessionsOlderThan(long tooOldTimestamp) {
		Set<MeetSession> tooOldSessions = new LinkedHashSet<MeetSession>();
		if (sessions.isEmpty()) {
			return;
		}
		synchronized (sessions) {
			for (MeetSession meetSession : sessions) {
				if (meetSession.getLastAliveSign() <= tooOldTimestamp) {
					System.out.println("tooOld: " + meetSession.getSessionId());
					tooOldSessions.add(meetSession);
				}
			}
			if (tooOldSessions.isEmpty()) {
				return;
			}
			for (MeetSession tooOldSession : tooOldSessions) {
				for (BiConsumer<MeetSession, Set<MeetSession>> oldSessionHandler : onOutgoingBrowserTabHandlers) {
					oldSessionHandler.accept(tooOldSession, sessions);
				}
			}
			sessions.removeAll(tooOldSessions);
		}
		synchronized (offererAnswererConnection) {
			Set<MeetConnection> removeConnections = new LinkedHashSet<MeetConnection>();
			for (MeetConnection connection : offererAnswererConnection) {
				if (tooOldSessions.contains(connection.getAnswerer()) || tooOldSessions.contains(connection.getOfferer())) {
					removeConnections.add(connection);
				}
			}
			offererAnswererConnection.removeAll(removeConnections);
		}
	}

	public Set<MeetSession> connectionsMissingToWhatSession(MeetSession fromWho) {
		Set<MeetSession> missing = new LinkedHashSet<>();
		for (MeetSession meetSession : sessions) {
			if (meetSession != fromWho) {
				boolean existing = false;
				for (MeetConnection meetConnection : offererAnswererConnection) {
					boolean leftToRight = meetConnection.getAnswerer() == fromWho && meetConnection.getOfferer() == meetSession;
					boolean rightToLeft = meetConnection.getAnswerer() == meetSession && meetConnection.getOfferer() == fromWho;
					if (leftToRight | rightToLeft) {
						existing = true;
						break;
					}
				}
				if (!existing) {
					missing.add(meetSession);
				}
			}
		}
		return missing;
	}

	public void notifyOfferSent(String sdp, MeetSession offerer, MeetSession answerer) {
		for (MeetConnection meetConnection : offererAnswererConnection) {
			if (meetConnection.getOfferer() == offerer && meetConnection.getAnswerer() == answerer) {
				meetConnection.setOfferedSDP(sdp);
			}
		}
	}

	public void notifyAnswerSent(String sdp, MeetSession offerer, MeetSession answerer) {
		for (MeetConnection meetConnection : offererAnswererConnection) {
			if (meetConnection.getOfferer() == offerer && meetConnection.getAnswerer() == answerer) {
				meetConnection.setAnsweredSDP(sdp);
			}
		}
	}

	public int count() {
		return sessions.size();
	}

	public String getDebugInfo() {
		String msg = count() + " Connections:";
		for (MeetConnection conn : offererAnswererConnection) {
			msg += "\n" + conn.getDebugInfo();
		}
		msg += "\n Sess: " + sessions.size() + ".";
		return msg;
	}

	public abstract class Handler extends SimpleImmutableEntry<String, BiFunction<String, String, String>>
			implements BiFunction<String, String, String> {
		public MeetSessions sessions = MeetSessions.this;

		public Handler(String key, BiFunction<String, String, String> value) {
			super(key, value);
		}

		@Override
		public BiFunction<String, String, String> getValue() {
			return this;
		}
	}

	public String execute(String command, String headerSessionUUID) {
		Set<Handler> candidates = new LinkedHashSet<MeetSessions.Handler>();
		for (Handler handler : registry) {
			if (command.startsWith(handler.getKey())) {
				candidates.add(handler);
			}
		}
		if (candidates.size() == 1) {
			return candidates.iterator().next().apply(command, headerSessionUUID);
		}
		throw new RuntimeException("Expected one handler candidate but got " + candidates.size());
	};

	public void addOffererAnswererConnection(MeetConnection mc){
		this.offererAnswererConnection.add(mc);
	}
}