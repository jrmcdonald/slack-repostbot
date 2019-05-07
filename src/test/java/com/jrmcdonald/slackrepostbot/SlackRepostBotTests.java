package com.jrmcdonald.slackrepostbot;

import static org.mockito.Mockito.when;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrmcdonald.slackrepostbot.bot.RepostBot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.User;

@SpringBootTest
@ActiveProfiles("slack")
@RunWith(SpringJUnit4ClassRunner.class)
public class SlackRepostBotTests {

    @Mock
    private WebSocketSession session;

    @Mock
    private SlackService slackService;

    @Autowired
    @InjectMocks
    private RepostBot bot;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        // Set up bot user
        User user = new User();
        user.setName("repostbot");
        user.setId("UEADGH12S");

        // Set up Slack RTM API
        when(slackService.getImChannelIds()).thenReturn(Arrays.asList("D1E79BACV", "C0NDSV5B8"));
        when(slackService.getCurrentUser()).thenReturn(user);
        when(slackService.getWebSocketUrl()).thenReturn("");
    }

    @Test
    public void When_MessageWithPattern_Then_InvokeOnReceiveMessageWithPattern() throws Exception {
        SlackMessage message = new SlackMessage("A1E78BACV", "U023BECGF", "example message <http://www.google.com|www.google.com> <http://www.bing.com|www.bing.com>");

        genericTest(message);

        System.out.println(Mockito.mockingDetails(session).printInvocations());
    }

    private void genericTest(final SlackMessage message) throws Exception {
        bot.handleTextMessage(session, new TextMessage(mapper.writeValueAsString(message)));
    }
}
