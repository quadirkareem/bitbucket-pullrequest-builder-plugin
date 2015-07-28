package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by nishio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketUser {
    private String username;
    private String displayName;

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return this.displayName;
    }

    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("username=").append(username).append(", displayName=").append(displayName)
            .toString();
    }

    public String toStringFormat() {
        return displayName + " [" + username + "]";
    }
}
