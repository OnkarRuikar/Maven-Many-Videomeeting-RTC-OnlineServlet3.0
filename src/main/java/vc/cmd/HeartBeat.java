package vc.cmd;

import vc.MeetSessions;
import vc.MeetSessions.Handler;
import vc.MeetConnection;
import vc.MeetSession;

public class HeartBeat extends Handler {

	public HeartBeat(MeetSessions meetSessions) {
		meetSessions.super("HEARTBEAT", null);
	}

	@Override
	public String apply(String command, String headerSessionUUID) {
		MeetSession session = sessions.getById(headerSessionUUID);
		if (session != null) {
			session.notifyAlive();
			synchronized (sessions) {
				for (MeetSession missingSession : sessions.connectionsMissingToWhatSession(session)) {
					MeetConnection c = new MeetConnection(session, missingSession);
					sessions.addOffererAnswererConnection(c);
					session.addMessage("CREATE_OFFER:" + c.getAnswerer().getSessionId());
				}
			}
			String result = "";
			for (String message : session.copyMessageQueueAndClear()) {
				result += message + "\n";
			}
			return result;
		}
		return null;
	}
}
