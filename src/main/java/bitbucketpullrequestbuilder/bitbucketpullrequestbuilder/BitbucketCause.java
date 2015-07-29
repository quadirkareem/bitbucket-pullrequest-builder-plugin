package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Cause;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nishio
 */
public class BitbucketCause extends Cause {
    private static final Logger LOG = LoggerFactory.getLogger(BitbucketCause.class.getName());
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("INIT: sourceBranch={}, targetBranch={}, repositoryOwner={},"
                + " repositoryName={}, pullRequestId={}, destinationRepositoryOwner={},"
                + " destinationRepositoryName={}, pullRequestTitle={}, sourceCommitHash={},"
                + " destinationCommitHash={}, buildStartCommentId={}", new Object[] { sourceBranch, targetBranch,
                repositoryOwner, repositoryName, pullRequestId, destinationRepositoryOwner, destinationRepositoryName,
                pullRequestTitle, sourceCommitHash, destinationCommitHash, buildStartCommentId });
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
        LOG.debug("sourceBranch={}", sourceBranch);
        return sourceBranch;
    }

    public String getTargetBranch() {
        LOG.debug("targetBranch={}", targetBranch);
        return targetBranch;
    }

    public String getRepositoryOwner() {
        LOG.debug("repositoryOwner={}", repositoryOwner);
        return repositoryOwner;
    }

    public String getRepositoryName() {
        LOG.debug("repositoryName={}", repositoryName);
        return repositoryName;
    }

    public String getPullRequestId() {
        LOG.debug("pullRequestId={}", pullRequestId);
        return pullRequestId;
    }

    public String getDestinationRepositoryOwner() {
        LOG.debug("destinationRepositoryOwner={}", destinationRepositoryOwner);
        return destinationRepositoryOwner;
    }

    public String getDestinationRepositoryName() {
        LOG.debug("destinationRepositoryName={}", destinationRepositoryName);
        return destinationRepositoryName;
    }

    public String getPullRequestTitle() {
        LOG.debug("pullRequestTitle={}", pullRequestTitle);
        return pullRequestTitle;
    }

    public String getSourceCommitHash() {
        LOG.debug("sourceCommitHash={}", sourceCommitHash);
        return sourceCommitHash;
    }

    public String getDestinationCommitHash() {
        LOG.debug("destinationCommitHash={}", destinationCommitHash);
        return destinationCommitHash;
    }

    public String getBuildStartCommentId() {
        LOG.debug("buildStartCommentId={}", buildStartCommentId);
        return buildStartCommentId;
    }

    @Override
    public String getShortDescription() {
        String description = "<a href=" + BITBUCKET_URL + this.getDestinationRepositoryOwner() + "/";
        description += this.getDestinationRepositoryName() + "/pull-request/" + this.getPullRequestId();
        description += ">#" + this.getPullRequestId() + " " + this.getPullRequestTitle() + "</a>";
        LOG.debug("description={}", description);
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
