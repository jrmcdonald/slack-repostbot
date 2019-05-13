package com.jrmcdonald.slackrepostbot.repository;

import com.jrmcdonald.slackrepostbot.model.Channel;
import org.springframework.data.repository.CrudRepository;

/**
 * ChannelRepository
 */
public interface ChannelRepository extends CrudRepository<Channel, String> {

}