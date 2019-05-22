package com.jrmcdonald.slackrepostbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.jrmcdonald.slackrepostbot.model.Channel;
import com.jrmcdonald.slackrepostbot.model.Link;
import com.jrmcdonald.slackrepostbot.repository.ChannelRepository;
import org.springframework.stereotype.Service;

/**
 * ChannelService
 */
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;

    public ChannelService(final ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    private Channel getChannel(final String channelId) {
        Channel channel = null;

        Optional<Channel> optChannel = channelRepository.findById(channelId);

        if (optChannel.isPresent()) {
            channel = optChannel.get();
        } else {
            channel = channelRepository.save(new Channel(channelId));
        }

        return channel;
    }

    private boolean isRepost(Channel channel, final String url) {
        return channel.getLinks().containsKey(url);
    }

    private Link processRepost(Channel channel, final String url) {
        Link repostedLink = channel.getLinks().get(url);

        if (repostedLink != null) {
            repostedLink.incrementCount();
            channelRepository.save(channel);
        }

        return repostedLink;
    }

    private Link processNewLink(Channel channel, final String url, final String userId) {
        Link newLink = new Link(url, userId);

        channel.addLink(newLink);
        channelRepository.save(channel);

        return newLink;
    }

    public List<Link> processLinks(final String channelId, final String userId,
            final List<String> links) {
        ArrayList<Link> reposts = new ArrayList<Link>();

        Channel channel = getChannel(channelId);

        for (String link : links) {
            if (isRepost(channel, link)) {
                reposts.add(processRepost(channel, link));
            } else {
                processNewLink(channel, link, userId);
            }
        }

        return reposts;
    }
}
