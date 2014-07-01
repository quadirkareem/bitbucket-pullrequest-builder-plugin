package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by nishio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestCommentAuthor {
	private String username;
	private String firstName;
	private String lastName;
	private String displayName;

	@JsonProperty("username")
	public String getUsername() {
		return username;
	}

	@JsonProperty("username")
	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty("first_name")
	public String getFirstName() {
		return this.firstName;
	}

	@JsonProperty("first_name")
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@JsonProperty("last_name")
	public String getLastName() {
		return lastName;
	}

	@JsonProperty("last_name")
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
		return new StringBuilder().append("username=").append(username)
				.append(", firstName=").append(firstName).append(", lastName=")
				.append(lastName).append(", displayName=").append(displayName)
				.toString();
	}
}
