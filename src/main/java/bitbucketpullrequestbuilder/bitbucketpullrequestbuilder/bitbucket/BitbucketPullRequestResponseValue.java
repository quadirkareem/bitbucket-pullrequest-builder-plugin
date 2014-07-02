package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by nishio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestResponseValue {
	private String description;
	private Boolean closeSourceBranch;
	private String title;
	private BitbucketPullRequestResponseValueRepository destination;
	private String reason;
	private BitbucketUser closedBy;
	private BitbucketPullRequestResponseValueRepository source;
	private String state;
	private String createdOn;
	private String updatedOn;
	private BitbucketPullRequestResponseValueRepositoryCommit mergeCommit;
	private String id;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("close_source_branch")
	public Boolean getCloseSourceBranch() {
		return closeSourceBranch;
	}

	@JsonProperty("close_source_branch")
	public void setCloseSourceBranch(Boolean closeSourceBranch) {
		this.closeSourceBranch = closeSourceBranch;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BitbucketPullRequestResponseValueRepository getDestination() {
		return destination;
	}

	public void setDestination(
			BitbucketPullRequestResponseValueRepository destination) {
		this.destination = destination;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@JsonProperty("closed_by")
	public BitbucketUser getClosedBy() {
		return closedBy;
	}

	@JsonProperty("closed_by")
	public void setClosedBy(BitbucketUser closedBy) {
		this.closedBy = closedBy;
	}

	public BitbucketPullRequestResponseValueRepository getSource() {
		return source;
	}

	public void setSource(BitbucketPullRequestResponseValueRepository source) {
		this.source = source;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("created_on")
	public String getCreatedOn() {
		return createdOn;
	}

	@JsonProperty("created_on")
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	@JsonProperty("updated_on")
	public String getUpdatedOn() {
		return updatedOn;
	}

	@JsonProperty("updated_on")
	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}

	@JsonProperty("merge_commit")
	public BitbucketPullRequestResponseValueRepositoryCommit getMergeCommit() {
		return mergeCommit;
	}

	@JsonProperty("merge_commit")
	public void setMergeCommit(
			BitbucketPullRequestResponseValueRepositoryCommit mergeCommit) {
		this.mergeCommit = mergeCommit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("id=").append(id).append(", title=")
				.append(title).append(", description=").append(description)
				.append("closeSourceBranch=").append(closeSourceBranch)
				.append(", destination=").append(destination)
				.append(", reason=").append(reason).append(", closedBy=")
				.append(closedBy).append(", source=").append(source)
				.append(", state=").append(state).append(", createdOn=")
				.append(createdOn).append(", updatedOn=").append(updatedOn)
				.append(", mergeCommit=").append(mergeCommit).toString();
	}
}
