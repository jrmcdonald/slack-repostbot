package com.jrmcdonald.slackrepostbot;

/**
 * SlackMessage
 */
public class SlackMessage {

    private String type;
    private String ts;
    private String channel;
    private String user;
    private String text;

    public SlackMessage(String user, String channel, String text) {
        this.type = "message";
        this.ts = "";
        this.channel = channel;
        this.user = user;
        this.text = text;
    }

    /**
     * @return the type
     */
    public String getType() {
      return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
      this.type = type;
    }

    /**
     * @return the ts
     */
    public String getTs() {
      return ts;
    }

    /**
     * @param ts the ts to set
     */
    public void setTs(String ts) {
      this.ts = ts;
    }

    /**
     * @return the channel
     */
    public String getChannel() {
      return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(String channel) {
      this.channel = channel;
    }
    
    /**
     * @return the user
     */
    public String getUser() {
      return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
      this.user = user;
    }

    /**
     * @return the text
     */
    public String getText() {
      return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
      this.text = text;
    }
}