package com.jrmcdonald.slackrepostbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.jrmcdonald.slackrepostbot.model.Channel;
import com.jrmcdonald.slackrepostbot.model.Link;
import com.jrmcdonald.slackrepostbot.repository.ChannelRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChannelServiceTest {

    private static final String GOOGLE_URL = "http://www.google.com";
    private static final String BING_URL = "http://www.bing.com";
    private static final String CHANNEL_ID = "U023BECGF";
    private static final String USER_ID = "A1E78BACV";

    @MockBean
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelService channelService;

    @Test
    public void givenChatMessageWithNewChannelAndNewLink_whenMessageIsProcessed_thenNewLinkIsStored()
            throws Exception {
        // build channel comparator
        Channel comparator = new ChannelBuilder(CHANNEL_ID).withLink(GOOGLE_URL, USER_ID).build();

        // set up repository responses and verify arguments passed to save
        doReturn(Optional.empty()).when(channelRepository).findById(CHANNEL_ID);
        doAnswer(i -> verifyChannelArgument(i, new Channel(CHANNEL_ID)))
                .doAnswer(i -> verifyChannelArgument(i, comparator)).when(channelRepository)
                .save(any(Channel.class));

        // fetch reposts
        List<Link> reposts =
                channelService.processLinks(CHANNEL_ID, USER_ID, Arrays.asList(GOOGLE_URL));

        // verify mock invocations
        verify(channelRepository, times(1)).findById(CHANNEL_ID);
        verify(channelRepository, times(2)).save(any(Channel.class));
        verifyNoMoreInteractions(channelRepository);

        // check no reposts
        assertThat(reposts.size()).isZero();
    }

    @Test
    public void givenChatMessageWithExistingChannelAndNewLink_whenMessageIsProcessed_thenNewLinkIsStored()
            throws Exception {
        // build channel comparator
        Channel comparator = new ChannelBuilder(CHANNEL_ID).withLink(GOOGLE_URL, USER_ID)
                .withLink(BING_URL, USER_ID).build();

        Channel source = new ChannelBuilder(CHANNEL_ID).withLink(GOOGLE_URL, USER_ID).build();

        // set up repository responses and verify arguments passed to save
        doReturn(Optional.of(source)).when(channelRepository).findById(CHANNEL_ID);
        doAnswer(i -> verifyChannelArgument(i, comparator)).when(channelRepository)
                .save(any(Channel.class));

        // fetch reposts
        List<Link> reposts =
                channelService.processLinks(CHANNEL_ID, USER_ID, Arrays.asList(BING_URL));

        // verify mock invocations
        verify(channelRepository, times(1)).findById(CHANNEL_ID);
        verify(channelRepository, times(1)).save(any(Channel.class));
        verifyNoMoreInteractions(channelRepository);

        // check no reposts
        assertThat(reposts.size()).isZero();
    }

    @Test
    public void givenChatMessageWithExistingChannelAndExistingLink_whenMessageIsProcessed_thenPostCountIsIncremented()
            throws Exception {
        // build channels
        Channel source = new ChannelBuilder(CHANNEL_ID).withLink(BING_URL, USER_ID).build();

        Channel comparator = new ChannelBuilder(CHANNEL_ID).withLink(BING_URL, USER_ID, 2).build();

        // set up repository responses and verify arguments passed to save
        doReturn(Optional.of(source)).when(channelRepository).findById(CHANNEL_ID);
        doAnswer(i -> verifyChannelArgument(i, comparator)).when(channelRepository)
                .save(any(Channel.class));

        // fetch reposts
        List<Link> reposts =
                channelService.processLinks(CHANNEL_ID, USER_ID, Arrays.asList(BING_URL));

        // verify mock invocations
        verify(channelRepository, times(1)).findById(CHANNEL_ID);
        verify(channelRepository, times(1)).save(any(Channel.class));
        verifyNoMoreInteractions(channelRepository);

        // check that there is a repost
        assertThat(reposts.size()).isEqualTo(1);
        assertThat(reposts.get(0)).isEqualTo(comparator.getLinks().get(BING_URL));
    }

    private Channel verifyChannelArgument(InvocationOnMock invocation, Channel comparator) {
        Channel argument = invocation.getArgument(0);
        assertThat(argument).isEqualToComparingFieldByFieldRecursively(comparator);
        return argument;
    }

    class ChannelBuilder {

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
}
