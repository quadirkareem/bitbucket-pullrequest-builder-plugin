package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Cause;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by quadirkareem
 */
public class TriggerJobCause extends Cause {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerJobCause.class.getName());
    private final String upstreamJob;
    private final String pullRequestId;
    private final String requestedBy;

    public TriggerJobCause(String upstreamJob, String pullRequestId, String requestedBy) {
        LOG.debug("INIT: upstreamJob=%s, pullRequestId=%s, requestedBy=%s", new Object[] { upstreamJob, pullRequestId,
            requestedBy });
        this.upstreamJob = upstreamJob;
        this.pullRequestId = pullRequestId;
        this.requestedBy = requestedBy;
    }

    public String getSourceBranch() {
        LOG.debug("upstreamJob={}", upstreamJob);
        return upstreamJob;
    }

    public String getPullRequestId() {
        LOG.debug("pullRequestId={}", pullRequestId);
        return pullRequestId;
    }

    public String getRequestedBy() {
        LOG.debug("requestedBy={}", requestedBy);
        return requestedBy;
    }

    @Override
    public String getShortDescription() {
        LOG.debug("shortDescription={}", toString());
        return toString();
    }

    @Override
    public String toString() {
        return new StringBuilder("BitbucketCause [upstreamJob=").append(upstreamJob).append(", pullRequestId=")
            .append(pullRequestId).append(", requestedBy=").append(requestedBy).append("]").toString();
    }

}
