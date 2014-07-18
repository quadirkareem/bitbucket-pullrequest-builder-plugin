package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.Cause;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

/**
 * Created by nishio
 */
public class BitbucketBuilds {
	private static final Logger logger = Logger.getLogger(BitbucketBuilds.class
			.getName());
	private BitbucketBuildTrigger trigger;
	private BitbucketRepository repository;

	public BitbucketBuilds(BitbucketBuildTrigger trigger,
			BitbucketRepository repository) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(logger, "INIT");
		}
		this.trigger = trigger;
		this.repository = repository;
	}

	public BitbucketCause getCause(AbstractBuild build) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(
					logger,
					String.format("build displayName=%s",
							build.getDisplayName()));
		}
		Cause cause = build.getCause(BitbucketCause.class);
		if (cause == null || !(cause instanceof BitbucketCause)) {
			return null;
		}
		return (BitbucketCause) cause;
	}

	public void onStarted(AbstractBuild build) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(
					logger,
					String.format("build displayName=%s",
							build.getDisplayName()));
		}

		BitbucketCause cause = this.getCause(build);
		if (cause == null) {
			return;
		}
		try {
			build.setDescription(cause.getShortDescription());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Can't update build description", e);
		}
	}

	public void onCompleted(AbstractBuild build) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(
					logger,
					String.format("build displayName=%s",
							build.getDisplayName()));
		}
		BitbucketCause cause = this.getCause(build);
		if (cause == null) {
			return;
		}
		Result result = build.getResult();
		String rootUrl = Jenkins.getInstance().getRootUrl();
		String buildUrl = "";
		if (rootUrl == null) {
			buildUrl = " PLEASE SET JENKINS ROOT URL FROM GLOBAL CONFIGURATION "
					+ build.getUrl();
		} else {
			buildUrl = rootUrl + build.getUrl();
		}
		// repository.deletePullRequestComment(cause.getPullRequestId(),
		// cause.getBuildStartCommentId());
		repository.postFinishedComment(cause.getPullRequestId(),
				cause.getSourceCommitHash(), cause.getDestinationCommitHash(),
				result == Result.SUCCESS, buildUrl);
	}

}
