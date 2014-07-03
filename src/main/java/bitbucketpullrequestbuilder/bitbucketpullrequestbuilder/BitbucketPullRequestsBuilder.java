package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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
		logger.info("BitbucketPullRequestsBuilder.getBuilder()");
		return new BitbucketPullRequestsBuilder();
	}

	public void stop() {
		logger.info("BitbucketPullRequestsBuilder.stop()");
		// TODO?
	}

	public void run() {
		logger.info("BitbucketPullRequestsBuilder.run()");
		logger.info("Build Start.");
		this.repository.init();
		logProjectProperties();
		Collection<BitbucketPullRequest> targetPullRequests = this.repository
				.getTargetPullRequests();
		this.repository.addFutureBuildTasks(targetPullRequests);
	}

	private void logProjectProperties() {
		logger.info("BitbucketPullRequestsBuilder.logProjectProperties()");
		ArrayList<ParameterValue> values = new ArrayList<ParameterValue>();
		ParametersDefinitionProperty pdp = this.project
				.getProperty(ParametersDefinitionProperty.class);
		if (pdp != null) {
			for (ParameterDefinition pd : pdp.getParameterDefinitions()) {
				ParameterValue pv = pd.getDefaultParameterValue();
				logger.info("paramdef.name=" + pd.getName()
						+ "; paramdef.type=" + pd.getType()
						+ "; paramdef.description=" + pd.getDescription()
						+ "; paramval.name=" + pv.getName() + "; paramval="
						+ pv.getDescription());
			}
		} else {
			logger.info("ParametersDefinitionProperty is null");
		}

		Map<JobPropertyDescriptor, ?> jobProperties = this.project
				.getProperties();
		if (jobProperties != null) {
			for (JobPropertyDescriptor jd : jobProperties.keySet()) {
				// Object t = jobProperties.get(jd);
				logger.info(jd.getT().toString());
			}
		} else {
			logger.info("jobProperties is null");
		}

	}

	public BitbucketPullRequestsBuilder setupBuilder() {
		logger.info("BitbucketPullRequestsBuilder.setupBuilder()");
		if (this.project == null || this.trigger == null) {
			throw new IllegalStateException();
		}
		this.repository = new BitbucketRepository(
				this.trigger.getProjectPath(), this);
		this.builds = new BitbucketBuilds(this.trigger, this.repository);
		return this;
	}

	public void setProject(AbstractProject<?, ?> project) {
		logger.info("BitbucketPullRequestsBuilder.setProject(): project displayName="
				+ project.getDisplayName());
		this.project = project;
	}

	public void setTrigger(BitbucketBuildTrigger trigger) {
		logger.info("BitbucketPullRequestsBuilder.setTrigger(): trigger projectPath="
				+ trigger.getProjectPath());
		this.trigger = trigger;
	}

	public AbstractProject<?, ?> getProject() {
		logger.info("BitbucketPullRequestsBuilder.getProject()");
		return this.project;
	}

	public BitbucketBuildTrigger getTrigger() {
		logger.info("BitbucketPullRequestsBuilder.getTrigger()");
		return this.trigger;
	}

	public BitbucketBuilds getBuilds() {
		logger.info("BitbucketPullRequestsBuilder.getBuilds()");
		return this.builds;
	}
}
