package com.jrmcdonald.slackrepostbot.util;

public class SlackMessageBuilder {

    private String channelId;
    private String userId;
    private String message;

    public SlackMessageBuilder() {
    }

    public SlackMessageBuilder withChannel(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public SlackMessageBuilder withUser(String userId) {
        this.userId = userId;
        return this;
    }

    public SlackMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public SlackMessage build() {
        return new SlackMessage(userId, channelId, message);
    }
}
