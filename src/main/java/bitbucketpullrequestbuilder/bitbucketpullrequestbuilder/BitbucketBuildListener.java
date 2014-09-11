package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Created by nishio
 */
@Extension
public class BitbucketBuildListener extends RunListener<AbstractBuild> {
	private static final Logger logger = Logger
			.getLogger(BitbucketBuildListener.class.getName());

	@Override
	public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(
					logger,
					String.format("build displayName=%s",
							abstractBuild.getDisplayName()));
		}
		// logger.info("BuildListener onStarted called.");
		BitbucketBuildTrigger trigger = BitbucketBuildTrigger
				.getTrigger(abstractBuild.getProject());
		if (trigger == null) {
			return;
		}
		trigger.getBuilder().getBuilds().onStarted(abstractBuild);
	}

	@Override
	public void onCompleted(AbstractBuild abstractBuild,
			@Nonnull TaskListener listener) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(
					logger,
					String.format("build displayName=%s",
							abstractBuild.getDisplayName()));
		}
		BitbucketBuildTrigger trigger = BitbucketBuildTrigger
				.getTrigger(abstractBuild.getProject());
		if (trigger == null) {
			return;
		}
		trigger.getBuilder().getBuilds().onCompleted(abstractBuild);
	}
}
