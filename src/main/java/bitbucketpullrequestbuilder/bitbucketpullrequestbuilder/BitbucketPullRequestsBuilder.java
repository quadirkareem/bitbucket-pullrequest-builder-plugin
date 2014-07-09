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
		logger.finer("INIT");
		return new BitbucketPullRequestsBuilder();
	}

	public void stop() {
		logger.info("Stopping ...");
	}

	public void run() {
		logger.info("Running ...");
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
		logger.finer("project displayName=" + project.getDisplayName());
		this.project = project;
	}

	public void setTrigger(BitbucketBuildTrigger trigger) {
		logger.finer("trigger projectPath=" + trigger.getProjectPath());
		this.trigger = trigger;
	}

	public AbstractProject<?, ?> getProject() {
		logger.finer("project displayName=" + this.project.getDisplayName());
		return this.project;
	}

	public BitbucketBuildTrigger getTrigger() {
		logger.finer("==");
		return this.trigger;
	}

	public BitbucketBuilds getBuilds() {
		logger.finer("==");
		return this.builds;
	}
}
