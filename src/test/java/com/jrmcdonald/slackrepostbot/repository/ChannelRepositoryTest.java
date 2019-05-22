package com.jrmcdonald.slackrepostbot.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import com.jrmcdonald.slackrepostbot.model.Channel;
import com.jrmcdonald.slackrepostbot.util.ChannelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ChannelRepositoryTest {

    private static final String GOOGLE_URL = "http://www.google.com";
    private static final String CHANNEL_ID = "U023BECGF";
    private static final String USER_ID = "A1E78BACV";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    public void givenNewChannelWithLinks_whenPersistedViaEntityManager_thenCanBeRetrievedByRepository() {
        Channel source = new ChannelBuilder(CHANNEL_ID).withLink(GOOGLE_URL, USER_ID).build();

        entityManager.persistAndFlush(source);

        Optional<Channel> comparator = channelRepository.findById(CHANNEL_ID);

        assertThat(comparator.isPresent()).isTrue();
        assertThat(comparator.get()).isEqualToComparingFieldByFieldRecursively(source);
    }

    @Test
    public void givenNewChannelWithLinks_whenPersistedViaRepository_thenCanBeRetrievedByEntityManager() {
        Channel source = new ChannelBuilder(CHANNEL_ID).withLink(GOOGLE_URL, USER_ID).build();

        channelRepository.save(source);

        Channel comparator = entityManager.find(Channel.class, CHANNEL_ID);

        assertThat(comparator).isNotNull();
        assertThat(comparator).isEqualToComparingFieldByFieldRecursively(source);
    }
}
