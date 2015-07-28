package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Cause;

import java.util.logging.Logger;

/**
 * Created by nishio
 */
public class BitbucketCause extends Cause {
    private static final Logger logger = Logger.getLogger(BitbucketCause.class.getName());
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

    public BitbucketCause(String sourceBranch, String targetBranch, String repositoryOwner, String repositoryName,
        String pullRequestId, String destinationRepositoryOwner, String destinationRepositoryName,
        String pullRequestTitle, String sourceCommitHash, String destinationCommitHash, String buildStartCommentId) {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            BitbucketPluginLogger
                .debug(
                    logger,
                    String
                        .format(
                            "INIT: sourceBranch=%s, targetBranch=%s, repositoryOwner=%s,"
                            + " repositoryName=%s, pullRequestId=%s, destinationRepositoryOwner=%s,"
                            + " destinationRepositoryName=%s, pullRequestTitle=%s, sourceCommitHash=%s,"
                            + " destinationCommitHash=%s, buildStartCommentId=%s",
                            sourceBranch, targetBranch, repositoryOwner, repositoryName, pullRequestId,
                            destinationRepositoryOwner, destinationRepositoryName, pullRequestTitle, sourceCommitHash,
                            destinationCommitHash, buildStartCommentId));
        }
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
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, sourceBranch);
        return sourceBranch;
    }

    public String getTargetBranch() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, targetBranch);
        return targetBranch;
    }

    public String getRepositoryOwner() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, repositoryOwner);
        return repositoryOwner;
    }

    public String getRepositoryName() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, repositoryName);
        return repositoryName;
    }

    public String getPullRequestId() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, pullRequestId);
        return pullRequestId;
    }

    public String getDestinationRepositoryOwner() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, destinationRepositoryOwner);
        return destinationRepositoryOwner;
    }

    public String getDestinationRepositoryName() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, destinationRepositoryName);
        return destinationRepositoryName;
    }

    public String getPullRequestTitle() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, pullRequestTitle);
        return pullRequestTitle;
    }

    public String getSourceCommitHash() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, sourceCommitHash);
        return sourceCommitHash;
    }

    public String getDestinationCommitHash() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, destinationCommitHash);
        return destinationCommitHash;
    }

    public String getBuildStartCommentId() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, buildStartCommentId);
        return buildStartCommentId;
    }

    @Override
    public String getShortDescription() {
        String description = "<a href=" + BITBUCKET_URL + this.getDestinationRepositoryOwner() + "/";
        description += this.getDestinationRepositoryName() + "/pull-request/" + this.getPullRequestId();
        description += ">#" + this.getPullRequestId() + " " + this.getPullRequestTitle() + "</a>";
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, description);
        return description;
    }

    @Override
    public String toString() {
        return new StringBuilder("BitbucketCause [sourceBranch=").append(sourceBranch).append(", targetBranch=")
            .append(targetBranch).append(", repositoryOwner=").append(repositoryOwner).append(", repositoryName=")
            .append(repositoryName).append(", pullRequestId=").append(pullRequestId)
            .append(", destinationRepositoryOwner=").append(destinationRepositoryOwner)
            .append(", destinationRepositoryName=").append(destinationRepositoryName).append(", pullRequestTitle=")
            .append(pullRequestTitle).append(", sourceCommitHash=").append(sourceCommitHash)
            .append(", destnationCommitHash=").append(destinationCommitHash).append(", buildStartCommentId=")
            .append(buildStartCommentId).append("]").toString();
    }

}
