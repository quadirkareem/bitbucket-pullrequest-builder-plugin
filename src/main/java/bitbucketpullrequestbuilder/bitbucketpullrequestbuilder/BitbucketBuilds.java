package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Cause;

import java.io.IOException;

import jenkins.model.Jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nishio
 */
public class BitbucketBuilds {
    private static final Logger LOG = LoggerFactory.getLogger(BitbucketBuilds.class.getName());
    private BitbucketBuildTrigger trigger;
    private BitbucketRepository repository;

    public BitbucketBuilds(BitbucketBuildTrigger trigger, BitbucketRepository repository) {
        LOG.debug("Initialising BitbucketBuilds Instance");
        this.trigger = trigger;
        this.repository = repository;
    }

    public BitbucketCause getCause(AbstractBuild build) {
        LOG.debug("build displayName={}", build.getDisplayName());
        Cause cause = build.getCause(BitbucketCause.class);
        if (cause == null || !(cause instanceof BitbucketCause)) {
            return null;
        }
        return (BitbucketCause) cause;
    }

    public void onStarted(AbstractBuild build) {
        LOG.debug("build displayName={}", build.getDisplayName());
        BitbucketCause cause = this.getCause(build);
        if (cause == null) {
            return;
        }
        try {
            build.setDescription(cause.getShortDescription());
        }
        catch (IOException e) {
            LOG.error("Can't update build description", e);
        }
    }

    public void onCompleted(AbstractBuild build) {
        LOG.debug("build displayName={}", build.getDisplayName());
        BitbucketCause cause = this.getCause(build);
        if (cause == null) {
            return;
        }
        Result result = build.getResult();
        String rootUrl = Jenkins.getInstance().getRootUrl();
        String buildUrl = "";
        if (rootUrl == null) {
            buildUrl = " PLEASE SET JENKINS ROOT URL FROM GLOBAL CONFIGURATION " + build.getUrl();
        }
        else {
            buildUrl = rootUrl + build.getUrl();
        }
        // repository.deletePullRequestComment(cause.getPullRequestId(),
        // cause.getBuildStartCommentId());
        repository.postFinishedComment(cause.getPullRequestId(), cause.getSourceCommitHash(),
            cause.getDestinationCommitHash(), result == Result.SUCCESS, buildUrl);
    }

}
