package com.jrmcdonald.slackrepostbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrmcdonald.slackrepostbot.model.Channel;
import com.jrmcdonald.slackrepostbot.model.Link;
import com.jrmcdonald.slackrepostbot.repository.ChannelRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChannelServiceTest {

    private static final String CHANNEL_ID = "U023BECGF";
    private static final String USER_ID = "A1E78BACV";

    @MockBean
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelService channelService;

    @Captor
    ArgumentCaptor<Channel> channelCaptor;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void givenChatMessageWithNewChannelAndNewLink_whenMessageIsProcessed_thenNewLinkIsStored() throws Exception {
        Channel comparatorChannel = new Channel(CHANNEL_ID);
        Link link = new Link("http://www.google.com", USER_ID);
        comparatorChannel.addLink(link);

        // set up repository responses
        doReturn(Optional.empty()).when(channelRepository).findById(CHANNEL_ID);
        doAnswer(invocation -> {
                Channel channelArgument = invocation.getArgument(0);
                assertThat(channelArgument).isEqualToComparingFieldByFieldRecursively(new Channel(CHANNEL_ID));
                return channelArgument;
        }).doAnswer(invocation -> {
            Channel channelArgument = invocation.getArgument(0);
            assertThat(channelArgument).isEqualToComparingFieldByFieldRecursively(comparatorChannel);
            return channelArgument;
        }).when(channelRepository).save(any(Channel.class));

        // fetch reposts
        List<Link> reposts = channelService.processLinks(CHANNEL_ID, USER_ID, Arrays.asList("http://www.google.com"));

        // verify mock invocations
        verify(channelRepository, times(1)).findById(CHANNEL_ID);
        verify(channelRepository, times(2)).save(channelCaptor.capture());
        verifyNoMoreInteractions(channelRepository);

        // check no reposts
        assertThat(reposts.size()).isZero();

        List<Channel> channels = channelCaptor.getAllValues();
        assertThat(channels.size()).isEqualTo(2);
        assertThat(channels.get(0)).isEqualToComparingFieldByFieldRecursively(new Channel(CHANNEL_ID));
        assertThat(channels.get(1)).isEqualToComparingFieldByFieldRecursively(comparatorChannel);
    }

    @Test
    public void givenChatMessageWithExistingChannelAndNewLink_whenMessageIsProcessed_thenNewLinkIsStored() throws Exception {
        // set up repository responses
        Channel sourceChannel = new Channel(CHANNEL_ID);
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(sourceChannel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(i -> i.getArguments()[0]);

        // fetch reposts
        List<Link> reposts = channelService.processLinks(CHANNEL_ID, USER_ID, Arrays.asList("http://www.bing.com"));

        // verify mock invocations
        verify(channelRepository, times(1)).findById(CHANNEL_ID);
        verify(channelRepository, times(1)).save(channelCaptor.capture());
        verifyNoMoreInteractions(channelRepository);

        // check no reposts
        assertThat(reposts.size()).isZero();

        // compare the channel object saved to the mock repository
        Channel comparatorChannel = new Channel(CHANNEL_ID);
        Link link = new Link("http://www.bing.com", USER_ID);
        comparatorChannel.addLink(link);

        List<Channel> channels = channelCaptor.getAllValues();
        assertThat(channels.size()).isEqualTo(1);
        assertThat(channels.get(0)).isEqualToComparingFieldByFieldRecursively(comparatorChannel);
    }
}
