package com.jrmcdonald.slackrepostbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Link
 */
@Entity
public class Link {

    @Id
    private String url;

    private String poster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    private Long count;

    protected Link() {}

    public Link(final String url, final String poster) {
        this.url = url;
        this.poster = poster;
        this.count = 0l;
    }
    
    /**
     * @return the url
     */
    public String getUrl() {
      return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
      this.url = url;
    }

    /**
     * @return the poster
     */
    public String getPoster() {
      return poster;
    }

    /**
     * @param poster the poster to set
     */
    public void setPoster(String poster) {
      this.poster = poster;
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
      return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(Channel channel) {
      this.channel = channel;
    }

    /**
     * @return the count
     */
    public Long getCount() {
      return count;
    }

    /**
     * Incremement the post count.
     */
    public void incrementCount() {
        this.count += 1;
    }

    /**
     * @param count the count to set
     */
    public void setCount(Long count) {
      this.count = count;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(url).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Link))
            return false;
        if (obj == this)
            return true;

        Link rhs = (Link) obj;
        return new EqualsBuilder().
            append(url, rhs.url).
            isEquals();
    }
}