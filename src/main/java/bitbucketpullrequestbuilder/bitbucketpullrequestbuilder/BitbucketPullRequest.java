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

	public BitbucketPullRequest(Operation Operation,
			BitbucketPullRequestResponseValue pullRequest, BitbucketUser commentAuthor) {
		this.operation = Operation;
		this.pullRequest = pullRequest;
		this.commentAuthor = commentAuthor;
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

}
