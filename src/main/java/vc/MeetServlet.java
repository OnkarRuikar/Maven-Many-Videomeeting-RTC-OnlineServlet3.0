package vc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/meet", name = "meetServlet")
public class MeetServlet extends HttpServlet {

	@Override
	public void destroy() {
		removeOldSessionsThread.interrupt();
	}

	private final MeetSessions sessions = new MeetSessions();

	private Thread removeOldSessionsThread = new Thread(() -> {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException ie) {
				// Maybe shutdown called.
				break;
			}
			try {
				//need bigger timeout. 2sec is too short.
				sessions.removeSessionsOlderThan(System.currentTimeMillis() - 10_000);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	});

	public MeetServlet() {
		super();
		sessions.addOnIncommingBrowserTabHandler((me, others) -> {
			for (MeetSession other : others) {
				if (me != other) {
					other.addMessage("ADD:" + me.getSessionId());
					me.addMessage("ADD:" + other.getSessionId());
				}
			}
		});
		sessions.addTooOldBrowserTabHandler((me, others) -> {
			for (MeetSession other : others) {
				if (me != other) {
					other.addMessage("REM:" + me.getSessionId());
					me.addMessage("REM:" + other.getSessionId());
				}
			}
		});
		removeOldSessionsThread.start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("refresh", "3");
		resp.getWriter().write(sessions.getDebugInfo());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String command = req.getReader().readLine();
		String headerSessionUUID = req.getHeader("X-session");
		String result = sessions.execute(command, headerSessionUUID);
		if (result != null) {
			resp.getWriter().write(result);
		}
	}
}
