package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Cause;

import java.util.logging.Logger;

/**
 * Created by nishio
 */
public class BitbucketCause extends Cause {
	private static final Logger logger = Logger.getLogger(BitbucketCause.class
			.getName());
	private final String sourceBranch;
	private final String targetBranch;
	private final String repositoryOwner;
	private final String repositoryName;
	private final String pullRequestId;
	private final String destinationRepositoryOwner;
	private final String destinationRepositoryName;
	private final String pullRequestTitle;
	private final String sourceCommitHash;
	private final String destinationCommitHash;
	private final String buildStartCommentId;
	public static final String BITBUCKET_URL = "https://bitbucket.org/";

	public BitbucketCause(String sourceBranch, String targetBranch,
			String repositoryOwner, String repositoryName,
			String pullRequestId, String destinationRepositoryOwner,
			String destinationRepositoryName, String pullRequestTitle,
			String sourceCommitHash, String destinationCommitHash,
			String buildStartCommentId) {
		logger.info("INIT: BitbucketCause(): sourceBranch=" + sourceBranch
				+ ", targetBranch=" + targetBranch + ", repositoryOwner="
				+ repositoryOwner + ", repositoryName=" + repositoryName
				+ ", pullRequestId=" + pullRequestId
				+ ", destinationRepositoryOwner=" + destinationRepositoryOwner
				+ ", destinationRepositoryName=" + destinationRepositoryName
				+ ", pullRequestTitle=" + pullRequestTitle
				+ ", sourceCommitHash=" + sourceCommitHash
				+ ", destnationCommitHash=" + destinationCommitHash
				+ ", buildStartCommentId=" + buildStartCommentId);
		this.sourceBranch = sourceBranch;
		this.targetBranch = targetBranch;
		this.repositoryOwner = repositoryOwner;
		this.repositoryName = repositoryName;
		this.pullRequestId = pullRequestId;
		this.destinationRepositoryOwner = destinationRepositoryOwner;
		this.destinationRepositoryName = destinationRepositoryName;
		this.pullRequestTitle = pullRequestTitle;
		this.sourceCommitHash = sourceCommitHash;
		this.destinationCommitHash = destinationCommitHash;
		this.buildStartCommentId = buildStartCommentId;
	}

	public String getSourceBranch() {
		logger.info("BitbucketCause.getSourceBranch()");
		return sourceBranch;
	}

	public String getTargetBranch() {
		logger.info("BitbucketCause.getTargetBranch()");
		return targetBranch;
	}

	public String getRepositoryOwner() {
		logger.info("BitbucketCause.getRepositoryOwner()");
		return repositoryOwner;
	}

	public String getRepositoryName() {
		logger.info("BitbucketCause.getRepositoryName()");
		return repositoryName;
	}

	public String getPullRequestId() {
		logger.info("BitbucketCause.getPullRequestId()");
		return pullRequestId;
	}

	public String getDestinationRepositoryOwner() {
		logger.info("BitbucketCause.getDestinationRepositoryOwner()");
		return destinationRepositoryOwner;
	}

	public String getDestinationRepositoryName() {
		logger.info("BitbucketCause.getDestinationRepositoryName()");
		return destinationRepositoryName;
	}

	public String getPullRequestTitle() {
		logger.info("BitbucketCause.getPullRequestTitle()");
		return pullRequestTitle;
	}

	public String getSourceCommitHash() {
		logger.info("BitbucketCause.getSourceCommitHash()");
		return sourceCommitHash;
	}

	public String getDestinationCommitHash() {
		logger.info("BitbucketCause.getDestinationCommitHash()");
		return destinationCommitHash;
	}

	public String getBuildStartCommentId() {
		logger.info("BitbucketCause.getBuildStartCommentId()");
		return buildStartCommentId;
	}

	@Override
	public String getShortDescription() {
		logger.info("BitbucketCause.getShortDescription()");
		String description = "<a href=" + BITBUCKET_URL
				+ this.getDestinationRepositoryOwner() + "/";
		description += this.getDestinationRepositoryName() + "/pull-request/"
				+ this.getPullRequestId();
		description += ">#" + this.getPullRequestId() + " "
				+ this.getPullRequestTitle() + "</a>";
		return description;
	}
}
