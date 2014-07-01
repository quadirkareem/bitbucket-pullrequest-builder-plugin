package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.model.queue.QueueTaskFuture;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import antlr.ANTLRException;

/**
 * Created by nishio
 */
public class BitbucketBuildTrigger extends Trigger<AbstractProject<?, ?>> {
	private static final Logger logger = Logger
			.getLogger(BitbucketBuildTrigger.class.getName());
	private final String projectPath;
	private final String cron;
	private final String username;
	private final String password;
	private final String repositoryOwner;
	private final String repositoryName;
	private final String targetBranch;
	private final Set<String> admins;
	private final String ciSkipPhrases;
	transient private BitbucketPullRequestsBuilder bitbucketPullRequestsBuilder;

	@Extension
	public static final BitbucketBuildTriggerDescriptor descriptor = new BitbucketBuildTriggerDescriptor();

	@DataBoundConstructor
	public BitbucketBuildTrigger(String projectPath, String cron,
			String username, String password, String repositoryOwner,
			String repositoryName, String targetBranch, Set<String> admins,
			String ciSkipPhrases) throws ANTLRException {
		super(cron);
		logger.info("INIT: BitbucketBuildTrigger(): projectPath=" + projectPath
				+ ", cron=" + cron + ", username=" + username + ", password="
				+ password + ", repositoryOwner=" + repositoryOwner
				+ ", repositoryName=" + repositoryName + ", targetBranch="
				+ targetBranch + ", admins="
				+ Arrays.toString(admins.toArray(new String[admins.size()]))
				+ ", ciSkipPhrases=" + ciSkipPhrases);
		this.projectPath = projectPath;
		this.cron = cron.trim();
		this.username = username.trim();
		this.password = password.trim();
		this.repositoryOwner = repositoryOwner.trim();
		this.repositoryName = repositoryName.trim();
		this.targetBranch = targetBranch.trim().toLowerCase();
		this.admins = admins;
		this.ciSkipPhrases = ciSkipPhrases;
	}

	public String getProjectPath() {
		logger.info("BitbucketBuildTrigger.getProjectPath()");
		return this.projectPath;
	}

	public String getCron() {
		logger.info("BitbucketBuildTrigger.getCron()");
		return this.cron;
	}

	public String getUsername() {
		logger.info("BitbucketBuildTrigger.getUsername()");
		return username;
	}

	public String getPassword() {
		logger.info("BitbucketBuildTrigger.getPassword()");
		return password;
	}

	public String getRepositoryOwner() {
		logger.info("BitbucketBuildTrigger.getRepositoryOwner()");
		return repositoryOwner;
	}

	public String getRepositoryName() {
		logger.info("BitbucketBuildTrigger.getRepositoryName()");
		return repositoryName;
	}

	public String getTargetBranch() {
		logger.info("BitbucketBuildTrigger.getTargetBranch()");
		return targetBranch;
	}

	public Set<String> getAdmins() {
		logger.info("BitbucketBuildTrigger.getAdmins()");
		return admins;
	}

	public String getCiSkipPhrases() {
		logger.info("BitbucketBuildTrigger.getCiSkipPhrases()");
		return ciSkipPhrases;
	}

	@Override
	public void start(AbstractProject<?, ?> project, boolean newInstance) {
		logger.info("BitbucketBuildTrigger.start():  project displayName="
				+ project.getDisplayName() + ", newInstance=" + newInstance);
		try {
			this.bitbucketPullRequestsBuilder = BitbucketPullRequestsBuilder
					.getBuilder();
			this.bitbucketPullRequestsBuilder.setProject(project);
			this.bitbucketPullRequestsBuilder.setTrigger(this);
			this.bitbucketPullRequestsBuilder.setupBuilder();
		} catch (IllegalStateException e) {
			logger.log(Level.SEVERE, "Can't start trigger", e);
			return;
		}
		super.start(project, newInstance);
	}

	public static BitbucketBuildTrigger getTrigger(AbstractProject project) {
		logger.info("BitbucketBuildTrigger.getTrigger(): project displayName="
				+ project.getDisplayName());
		Trigger trigger = project.getTrigger(BitbucketBuildTrigger.class);
		return (BitbucketBuildTrigger) trigger;
	}

	public BitbucketPullRequestsBuilder getBuilder() {
		logger.info("BitbucketBuildTrigger.getBuilder()");
		return this.bitbucketPullRequestsBuilder;
	}

	public QueueTaskFuture<?> startJob(BitbucketCause cause) {
		logger.info("BitbucketBuildTrigger.startJob(): cause shortDescription="
				+ cause.getShortDescription());
		Map<String, ParameterValue> values = new HashMap<String, ParameterValue>();
		values.put("sourceBranch", new StringParameterValue("sourceBranch",
				cause.getSourceBranch()));
		values.put("targetBranch", new StringParameterValue("targetBranch",
				cause.getTargetBranch()));
		values.put("repositoryOwner", new StringParameterValue(
				"repositoryOwner", cause.getRepositoryOwner()));
		values.put("repositonyName", new StringParameterValue("repositoryName",
				cause.getRepositoryName()));
		values.put("pullRequestId", new StringParameterValue("pullRequestId",
				cause.getPullRequestId()));
		values.put(
				"destinationRepositoryOwner",
				new StringParameterValue("destinationRepositoryOwner", cause
						.getDestinationRepositoryOwner()));
		values.put(
				"destinationRepositoryName",
				new StringParameterValue("destinationRepositoryName", cause
						.getDestinationRepositoryName()));
		values.put("pullRequestTitle", new StringParameterValue(
				"pullRequestTitle", cause.getPullRequestTitle()));

		return this.job.scheduleBuild2(0, cause, new ParametersAction(
				new ArrayList(values.values())));
	}

	@Override
	public void run() {
		logger.info("BitbucketBuildTrigger.run()");
		if (this.getBuilder().getProject().isDisabled()) {
			logger.info("Build Skip.");
		} else {
			this.bitbucketPullRequestsBuilder.run();
		}
		this.getDescriptor().save();
	}

	@Override
	public void stop() {
		logger.info("BitbucketBuildTrigger.stop()");
		super.stop();
	}

	public static final class BitbucketBuildTriggerDescriptor extends
			TriggerDescriptor {
		public BitbucketBuildTriggerDescriptor() {
			logger.info("INIT: BitbucketBuildTriggerDescriptor()");
			load();
		}

		@Override
		public boolean isApplicable(Item item) {
			logger.info("BitbucketBuildTriggerDescriptor.isApplicable(): item displayName="
					+ item.getDisplayName());
			return true;
		}

		@Override
		public String getDisplayName() {
			logger.info("BitbucketBuildTriggerDescriptor.getDisplayName()");
			return "Bitbucket Pull Requests Builder";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			logger.info("BitbucketBuildTriggerDescriptor.configure(): req url="
					+ req.getRequestURLWithQueryString() + ", json="
					+ json.toString());
			save();
			return super.configure(req, json);
		}
	}
}
