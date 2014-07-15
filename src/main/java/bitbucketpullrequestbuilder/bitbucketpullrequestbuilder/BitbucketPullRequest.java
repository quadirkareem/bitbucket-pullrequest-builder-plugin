package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestResponseValue;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketUser;

public class BitbucketPullRequest {

	enum Operation {
		MERGE, BUILD, DECLINE
	};

	private Operation operation;
	private BitbucketPullRequestResponseValue pullRequest;
	private BitbucketUser commentAuthor;
	private String comment;

	public BitbucketPullRequest(Operation Operation,
			BitbucketPullRequestResponseValue pullRequest,
			BitbucketUser commentAuthor, String comment) {
		this.operation = Operation;
		this.pullRequest = pullRequest;
		this.commentAuthor = commentAuthor;
		this.comment = comment;
	}

	public Operation getOperation() {
		return operation;
	}

	public BitbucketPullRequestResponseValue getPullRequest() {
		return pullRequest;
	}

	public BitbucketUser getCommentAuthor() {
		return commentAuthor;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return new StringBuilder("BitbucketPullRequest [operation=")
				.append(operation).append(", pullRequest=").append(pullRequest)
				.append(", commentAuthor=").append(commentAuthor).append(", comment=")
				.append(comment).append("]").toString();
	}
}
