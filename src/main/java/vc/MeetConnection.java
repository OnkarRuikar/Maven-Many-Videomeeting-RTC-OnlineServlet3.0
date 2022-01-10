package vc;

public class MeetConnection {
  private final MeetSession offerer;
  private final MeetSession answerer;
  private String offeredSDP;
  private String answeredSDP;

  public MeetConnection(MeetSession offerer, MeetSession answerer) {
    this.offerer = offerer;
    this.answerer = answerer;
  }

  public MeetSession getOfferer() {
    return offerer;
  }

  public MeetSession getAnswerer() {
    return answerer;
  }

  public String getOfferedSDP() {
    return offeredSDP;
  }

  public void setOfferedSDP(String offererSDP) {
    this.offeredSDP = offererSDP;
  }

  public String getAnsweredSDP() {
    return answeredSDP;
  }

  public void setAnsweredSDP(String answererSDP) {
    this.answeredSDP = answererSDP;
  }

  public String getDebugInfo() {
    return "FROM " + offerer.getSessionId() + " to " + answerer.getSessionId() + " (offeredSDP:"
        + (offeredSDP != null) + ", answeredSDP:" + (answeredSDP != null) + ")";
  }
}