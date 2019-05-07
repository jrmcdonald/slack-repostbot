package com.jrmcdonald.slackrepostbot.bot;

import java.util.regex.Matcher;
import com.jrmcdonald.slackrepostbot.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.WebSocketSession;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.common.JBot;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.Event;

/**
 * RepostBot
 */
@JBot
@Profile("slack")
public class RepostBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(RepostBot.class);

    private final BotConfig config;

    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    public SlackService getSlackService() {
        return this.slackService;
    }

    @Autowired
    public RepostBot(BotConfig config) {
        this.config = config;
    }

    @Controller(events = EventType.MESSAGE)
    public void onReceiveMessage(WebSocketSession session, Event event) {
        Matcher matcher = config.getSlackLinkPattern().matcher(event.getText());

        while (matcher.find()) {
            String hyperlink = matcher.group(1);
            if (config.getValidUrlPattern().matcher(hyperlink).matches()) {
                logger.debug("Valid hyperlink found: {}", hyperlink);
            } else {
                logger.debug("{}, is not a valid hyperlink", hyperlink);
            }
        }
    }
}