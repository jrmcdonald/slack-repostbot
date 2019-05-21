package com.jrmcdonald.slackrepostbot.util;

import com.jrmcdonald.slackrepostbot.model.Channel;
import com.jrmcdonald.slackrepostbot.model.Link;

public class ChannelBuilder {

    private Channel channel;

    public ChannelBuilder(final String channelId) {
        this.channel = new Channel(channelId);
    }

    public ChannelBuilder withLink(final String url, final String userId) {
        this.channel.addLink(new Link(url, userId));
        return this;
    }

    public ChannelBuilder withLink(final String url, final String userId, long postCount) {
        Link link = new Link(url, userId);
        link.setCount(postCount);
        this.channel.addLink(link);
        return this;
    }

    public Channel build() {
        return this.channel;
    }
}
