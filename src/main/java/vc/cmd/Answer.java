package vc.cmd;

import vc.MeetSessions;
import vc.MeetSessions.Handler;
import vc.MeetSession;

public class Answer extends Handler {

	public Answer(MeetSessions meetSessions) {
		meetSessions.super("ANSWER:", null);
	}

	@Override
	public String apply(String command, String headerSessionUUID) {
		String sessionIdAndSDP = command.substring(getKey().length());
		int sep = sessionIdAndSDP.indexOf(':');
		String targetSessionId = sessionIdAndSDP.substring(0, sep);
		String sdp = sessionIdAndSDP.substring(sep + 1);
		return calculate(headerSessionUUID, targetSessionId, sdp);
	}

	private String calculate(String headerSessionUUID, String targetSessionId, String sdp) {
		MeetSession session = sessions.getById(headerSessionUUID);
		MeetSession target = sessions.getById(targetSessionId);
		if (target == null) {
			session.addMessage("FORGET_UNKNOWN_SESSION:" + targetSessionId);
		} else {
			target.addMessage("INCOMMING_ANSWER:" + headerSessionUUID + ":" + sdp);
			sessions.notifyAnswerSent(sdp, target, session);
		}
		return null;
	}
}
