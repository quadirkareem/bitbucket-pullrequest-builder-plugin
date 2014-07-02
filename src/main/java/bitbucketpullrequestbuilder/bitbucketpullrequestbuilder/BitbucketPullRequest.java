package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestResponseValue;

public class BitbucketPullRequest {

	enum Operation {
		MERGE, BUILD, DECLINE
	};

	private Operation operation;
	private BitbucketPullRequestResponseValue pullRequest;

	public BitbucketPullRequest(Operation Operation,
			BitbucketPullRequestResponseValue pullRequest) {
		this.operation = Operation;
		this.pullRequest = pullRequest;
	}

	public Operation getOperation() {
		return operation;
	}

	public BitbucketPullRequestResponseValue getPullRequest() {
		return pullRequest;
	}

}
