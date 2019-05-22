package com.jrmcdonald.slackrepostbot.repository;

import com.jrmcdonald.slackrepostbot.model.Channel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * ChannelRepository
 */
@Repository
public interface ChannelRepository extends CrudRepository<Channel, String> {

}
