package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Cause;

import java.util.logging.Logger;

/**
 * Created by quadirkareem
 */
public class TriggerJobCause extends Cause {
	private static final Logger logger = Logger.getLogger(TriggerJobCause.class
			.getName());
	private final String upstreamJob;
	private final String pullRequestId;
	private final String requestedBy;

	public TriggerJobCause(String upstreamJob, String pullRequestId,
			String requestedBy) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(logger, String.format(
					"INIT: upstreamJob=%s, pullRequestId=%s, requestedBy=%s",
					upstreamJob, pullRequestId, requestedBy));
		}
		this.upstreamJob = upstreamJob;
		this.pullRequestId = pullRequestId;
		this.requestedBy = requestedBy;
	}

	public String getSourceBranch() {
		BitbucketPluginLogger.debug(logger, upstreamJob);
		return upstreamJob;
	}

	public String getPullRequestId() {
		BitbucketPluginLogger.debug(logger, pullRequestId);
		return pullRequestId;
	}

	public String getRequestedBy() {
		BitbucketPluginLogger.debug(logger, requestedBy);
		return requestedBy;
	}

	@Override
	public String getShortDescription() {
		BitbucketPluginLogger.debug(logger, toString());
		return toString();
	}

	@Override
	public String toString() {
		return new StringBuilder("BitbucketCause [upstreamJob=")
				.append(upstreamJob).append(", pullRequestId=")
				.append(pullRequestId).append(", requestedBy=")
				.append(requestedBy).append("]").toString();
	}

}
