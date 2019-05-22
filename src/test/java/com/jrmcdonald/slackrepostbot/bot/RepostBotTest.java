package com.jrmcdonald.slackrepostbot.bot;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrmcdonald.slackrepostbot.config.BotConfig;
import com.jrmcdonald.slackrepostbot.model.Link;
import com.jrmcdonald.slackrepostbot.service.ChannelService;
import com.jrmcdonald.slackrepostbot.util.SlackMessage;
import com.jrmcdonald.slackrepostbot.util.SlackMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.User;

@ExtendWith(MockitoExtension.class)
public class RepostBotTest {

    private static final String CHANNEL_ID = "U023BECGF";
    private static final String USER_ID = "A1E78BACV";
    private static final String VALID_URL_PATTERN =
            "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)([).!';/?:,][[:blank:]])?$";
    private static final String SLACK_LINK_PATTERN = "<(.*?)(\\|.*?)?>";

    @Mock
    private WebSocketSession session;

    @Mock
    private SlackService slackService;

    @Mock
    private BotConfig config;

    @Mock
    private ChannelService channelService;

    private RepostBot bot;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void initEach() {
        bot = new RepostBot(config, channelService);
        ReflectionTestUtils.setField(bot, "slackService", slackService);

        // Set up bot user
        User user = new User();
        user.setName("repostbot");
        user.setId("UEADGH12S");

        // Set up Slack RTM API
        doReturn(Arrays.asList("D1E79BACV", "C0NDSV5B8")).when(slackService).getImChannelIds();
        doReturn(user).when(slackService).getCurrentUser();

        doReturn(Pattern.compile(SLACK_LINK_PATTERN)).when(config).getSlackLinkPattern();
    }

    @Test
    public void givenChatMessageWithLinks_whenMessageIsReceived_thenChannelServiceIsInvoked()
            throws Exception {
        SlackMessageBuilder builder = new SlackMessageBuilder();
        SlackMessage message = builder.withUser(USER_ID).withChannel(CHANNEL_ID).withMessage(
                "example message <http://www.google.com|www.google.com> <http://www.bing.com|www.bing.com>")
                .build();

        doReturn(Pattern.compile(VALID_URL_PATTERN)).when(config).getValidUrlPattern();
        doReturn(new ArrayList<Link>()).when(channelService).processLinks(anyString(), anyString(),
                anyList());

        genericTest(message);

        verify(channelService, times(1)).processLinks(eq(CHANNEL_ID), eq(USER_ID),
                eq(Arrays.asList("http://www.google.com", "http://www.bing.com")));
    }

    @Test
    public void givenChatMessageWithNoLinks_whenMessageIsReceived_thenChannelServiceIsNotInvoked()
            throws Exception {
        SlackMessageBuilder builder = new SlackMessageBuilder();
        SlackMessage message = builder.withUser(USER_ID).withChannel(CHANNEL_ID)
                .withMessage("example message").build();

        genericTest(message);

        verifyZeroInteractions(channelService);
    }

    private void genericTest(final SlackMessage message) throws Exception {
        bot.handleTextMessage(session, new TextMessage(mapper.writeValueAsString(message)));
    }
}
