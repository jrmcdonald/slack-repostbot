package com.jrmcdonald.slackrepostbot.model;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

/**
 * Channel
 */
@Entity
public class Channel {

    @Id
    private String id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "url")
    private Map<String, Link> links;

    protected Channel() {}

    public Channel(final String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the links
     */
    public Map<String, Link> getLinks() {
        if (links == null) 
            links = new HashMap<String, Link>();

        return links;
    }

    /**
     * @param links the links to set
     */
    public void setLinks(HashMap<String, Link> links) {
        this.links = links;
    }

    /**
     * @param link the link to add
     */
    public void addLink(Link link) {
        if (links == null)
            links = new HashMap<String, Link>();
            
        links.put(link.getUrl(), link);
        link.setChannel(this);
    }

    /**
     * @param link the link to remove
     */
    public void removeLink(Link link) {
        links.remove(link.getUrl());
        link.setChannel(null);
    }
}
