package com.jrmcdonald.slackrepostbot.config;

import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="repostbot")
public class BotConfig {

    @NotNull
    private Pattern slackLinkPattern;

    @NotNull
    private Pattern validUrlPattern;

    /**
     * @return the slackLinkPattern
     */
    public Pattern getSlackLinkPattern() {
      return slackLinkPattern;
    }

    /**
     * @param slackLinkPattern the slackLinkPattern to set
     */
    public void setSlackLinkPattern(String slackLinkPattern) {
      this.slackLinkPattern = Pattern.compile(slackLinkPattern);
    }

    /**
     * @return the validUrlPattern
     */
    public Pattern getValidUrlPattern() {
      return validUrlPattern;
    }

    /**
     * @param validUrlPattern the validUrlPattern to set
     */
    public void setValidUrlPattern(String validUrlPattern) {
      this.validUrlPattern = Pattern.compile(validUrlPattern);
    }
}