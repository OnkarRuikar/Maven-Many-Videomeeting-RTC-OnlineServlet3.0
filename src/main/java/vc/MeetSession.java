
package vc;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class MeetSession {
  private final UUID id;

  private Set<String> messages = new LinkedHashSet<>();

  private long lastAliveSign = System.currentTimeMillis();

  public MeetSession(UUID id) {
    this.id = id;
  }

  public UUID getSessionId() {
    return id;
  }

  public void notifyAlive() {
    lastAliveSign = System.currentTimeMillis();
  }

  public Set<String> copyMessageQueueAndClear() {
    synchronized (messages) {
      Set<String> tmpMsgs = new LinkedHashSet<>(messages);
      messages.clear();
      return tmpMsgs;
    }
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public long getLastAliveSign() {
    return lastAliveSign;
  }
}
