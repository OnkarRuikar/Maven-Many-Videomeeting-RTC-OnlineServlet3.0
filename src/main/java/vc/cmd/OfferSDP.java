package vc.cmd;

import vc.MeetSessions;
import vc.MeetSessions.Handler;
import vc.MeetSession;

public class OfferSDP extends Handler {

	public OfferSDP(MeetSessions meetSessions) {
		meetSessions.super("OFFER_SDP:", null);
	}

	@Override
	public String apply(String command, String headerSessionUUID) {
		MeetSession session = sessions.getById(headerSessionUUID);
		if (session == null) {
			return null;
		}
		String sessionIdAndSDP = command.substring(getKey().length());
		int sep = sessionIdAndSDP.indexOf(':');
		String targetSessionId = sessionIdAndSDP.substring(0, sep);
		String sdp = sessionIdAndSDP.substring(sep + 1);
		return calculate(headerSessionUUID, session, targetSessionId, sdp);
	}

	private String calculate(String headerSessionUUID, MeetSession session, String targetSessionId, String sdp) {
		MeetSession byId = sessions.getById(targetSessionId);
		if (byId == null) {
			session.addMessage("FORGET_UNKNOWN_SESSION:" + targetSessionId);
		} else {
			byId.addMessage("INCOMMING_OFFER:" + headerSessionUUID + ":" + sdp);
			sessions.notifyOfferSent(sdp, session, byId);
		}
		return null;
	}
}
