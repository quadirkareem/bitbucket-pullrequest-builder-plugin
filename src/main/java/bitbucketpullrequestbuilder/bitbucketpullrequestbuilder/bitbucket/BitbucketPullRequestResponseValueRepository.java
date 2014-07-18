package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by nishio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestResponseValueRepository {
	private BitbucketPullRequestResponseValueRepositoryRepository repository;
	private BitbucketPullRequestResponseValueRepositoryBranch branch;
	private BitbucketPullRequestResponseValueRepositoryCommit commit;

	@JsonProperty("repository")
	public BitbucketPullRequestResponseValueRepositoryRepository getRepository() {
		return repository;
	}

	@JsonProperty("repository")
	public void setRepository(
			BitbucketPullRequestResponseValueRepositoryRepository repository) {
		this.repository = repository;
	}

	@JsonProperty("branch")
	public BitbucketPullRequestResponseValueRepositoryBranch getBranch() {
		return branch;
	}

	@JsonProperty("branch")
	public void setBranch(
			BitbucketPullRequestResponseValueRepositoryBranch branch) {
		this.branch = branch;
	}

	@JsonProperty("commit")
	public BitbucketPullRequestResponseValueRepositoryCommit getCommit() {
		return commit;
	}

	@JsonProperty("commit")
	public void setCommit(
			BitbucketPullRequestResponseValueRepositoryCommit commit) {
		this.commit = commit;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("repository=").append(repository)
				.append(", branch=").append(branch).append(", commit=")
				.append(commit).toString();
	}

}
