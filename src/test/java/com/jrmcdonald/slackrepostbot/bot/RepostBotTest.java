package com.jrmcdonald.slackrepostbot.bot;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrmcdonald.slackrepostbot.model.Link;
import com.jrmcdonald.slackrepostbot.service.ChannelService;
import com.jrmcdonald.slackrepostbot.util.SlackMessage;
import com.jrmcdonald.slackrepostbot.util.SlackMessageBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.User;

@SpringBootTest
@ActiveProfiles("slack")
@RunWith(SpringJUnit4ClassRunner.class)
public class RepostBotTest {

    @MockBean
    private WebSocketSession session;

    @MockBean
    private SlackService slackService;

    @MockBean
    private ChannelService channelService;

    @Autowired
    private RepostBot bot;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        // Set up bot user
        User user = new User();
        user.setName("repostbot");
        user.setId("UEADGH12S");

        // Set up Slack RTM API
        doReturn(Arrays.asList("D1E79BACV", "C0NDSV5B8")).when(slackService).getImChannelIds();
        doReturn(user).when(slackService).getCurrentUser();
        doReturn("").when(slackService).getWebSocketUrl();
    }

    @Test
    public void givenChatMessageWithLinks_whenMessageIsReceived_thenChannelServiceIsInvoked()
            throws Exception {
        SlackMessageBuilder builder = new SlackMessageBuilder();
        SlackMessage message = builder.withUser("A1E78BACV").withChannel("U023BECGF").withMessage(
                "example message <http://www.google.com|www.google.com> <http://www.bing.com|www.bing.com>")
                .build();

        doReturn(new ArrayList<Link>()).when(channelService).processLinks(anyString(), anyString(),
                anyList());

        genericTest(message);

        verify(channelService, times(1)).processLinks(eq("U023BECGF"), eq("A1E78BACV"),
                eq(Arrays.asList("http://www.google.com", "http://www.bing.com")));
    }

    private void genericTest(final SlackMessage message) throws Exception {
        bot.handleTextMessage(session, new TextMessage(mapper.writeValueAsString(message)));
    }
}
