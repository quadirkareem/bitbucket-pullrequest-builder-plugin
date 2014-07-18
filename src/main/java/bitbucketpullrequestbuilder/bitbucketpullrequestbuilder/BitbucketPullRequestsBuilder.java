package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by nishio
 */
public class BitbucketPullRequestsBuilder {
	private static final Logger logger = Logger
			.getLogger(BitbucketBuildTrigger.class.getName());
	private AbstractProject<?, ?> project;
	private BitbucketBuildTrigger trigger;
	private BitbucketRepository repository;
	private BitbucketBuilds builds;

	public static BitbucketPullRequestsBuilder getBuilder() {
		BitbucketPluginLogger.debug(logger, "INIT");
		return new BitbucketPullRequestsBuilder();
	}

	public void stop() {
		logger.info("Stopping ...");
	}

	public void run() {
		logger.info(String.format("job=%s, Started ...",
				project.getDisplayName()));
		this.repository.init();
		Collection<BitbucketPullRequest> targetPullRequests = this.repository
				.getTargetPullRequests();
		this.repository.addFutureBuildTasks(targetPullRequests);
	}

	public BitbucketPullRequestsBuilder setupBuilder() {
		logger.info("Setting up Builder ...");
		if (this.project == null || this.trigger == null) {
			throw new IllegalStateException();
		}
		this.repository = new BitbucketRepository(
				this.trigger.getProjectPath(), this);
		this.builds = new BitbucketBuilds(this.trigger, this.repository);
		return this;
	}

	public void setProject(AbstractProject<?, ?> project) {
		BitbucketPluginLogger.debug(
				logger,
				String.format("project displayName=%s",
						project.getDisplayName()));
		this.project = project;
	}

	public void setTrigger(BitbucketBuildTrigger trigger) {
		BitbucketPluginLogger.debug(
				logger,
				String.format("trigger projectPath=%s",
						trigger.getProjectPath()));
		this.trigger = trigger;
	}

	public AbstractProject<?, ?> getProject() {
		BitbucketPluginLogger.debug(
				logger,
				String.format("project displayName=%s",
						this.project.getDisplayName()));
		return this.project;
	}

	public BitbucketBuildTrigger getTrigger() {
		BitbucketPluginLogger.debug(logger, "==");
		return this.trigger;
	}

	public BitbucketBuilds getBuilds() {
		BitbucketPluginLogger.debug(logger, "==");
		return this.builds;
	}
}
