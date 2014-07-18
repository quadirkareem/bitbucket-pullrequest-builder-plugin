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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
	private static final Pattern DELIMITER_PATTERN = Pattern
			.compile("(?s)[,;\\s]\\s*");
	private final String projectPath;
	private final String cron;
	private final String username;
	private final String password;
	private final String repositoryOwner;
	private final String repositoryName;
	private final String targetBranch;
	private final String admins;
	private final String ciSkipPhrases;
	private Set<String> adminsList;
	transient private BitbucketPullRequestsBuilder bitbucketPullRequestsBuilder;

	@Extension
	public static final BitbucketBuildTriggerDescriptor descriptor = new BitbucketBuildTriggerDescriptor();

	@DataBoundConstructor
	public BitbucketBuildTrigger(String projectPath, String cron,
			String username, String password, String repositoryOwner,
			String repositoryName, String targetBranch, String admins,
			String ciSkipPhrases) throws ANTLRException {
		super(cron);
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger
					.debug(logger,
							String.format(
									"INIT: projectPath=%s, cron=%s, username=%s, password=%s, repositoryOwner=%s, repositoryName=%s, targetBranch=%s, admins=%s, ciSkipPhrases=%s",
									projectPath, cron, username, password,
									repositoryOwner, repositoryName,
									targetBranch, admins, ciSkipPhrases));
		}
		this.projectPath = projectPath;
		this.cron = cron.trim();
		this.username = username.trim();
		this.password = password.trim();
		this.repositoryOwner = repositoryOwner.trim();
		this.repositoryName = repositoryName.trim();
		this.targetBranch = targetBranch.trim();
		this.admins = admins;
		this.ciSkipPhrases = ciSkipPhrases;
		this.setAdminsList();
	}

	public String getProjectPath() {
		BitbucketPluginLogger.debug(logger, projectPath);
		return projectPath;
	}

	public String getCron() {
		BitbucketPluginLogger.debug(logger, cron);
		return cron;
	}

	public String getUsername() {
		BitbucketPluginLogger.debug(logger, username);
		return username;
	}

	public String getPassword() {
		BitbucketPluginLogger.debug(logger, password);
		return password;
	}

	public String getRepositoryOwner() {
		BitbucketPluginLogger.debug(logger, repositoryOwner);
		return repositoryOwner;
	}

	public String getRepositoryName() {
		BitbucketPluginLogger.debug(logger, repositoryName);
		return repositoryName;
	}

	public String getTargetBranch() {
		BitbucketPluginLogger.debug(logger, targetBranch);
		return targetBranch;
	}

	public String getAdmins() {
		BitbucketPluginLogger.debug(logger, admins);
		return admins;
	}

	public Set<String> getAdminsList() {
		BitbucketPluginLogger.debug(logger, Arrays
				.toString((String[]) adminsList.toArray(new String[adminsList
						.size()])));
		return adminsList;
	}

	public String getCiSkipPhrases() {
		BitbucketPluginLogger.debug(logger, ciSkipPhrases);
		return ciSkipPhrases;
	}

	private void setAdminsList() {
		BitbucketPluginLogger.debug(logger, admins);
		if (admins == null || admins.trim().length() == 0) {
			adminsList = new HashSet<String>();
		} else {
			String[] users = DELIMITER_PATTERN.split(admins);
			adminsList = new HashSet<String>(users.length);
			for (String u : users) {
				adminsList.add(u.trim().toLowerCase());
			}
		}
	}

	@Override
	public void start(AbstractProject<?, ?> project, boolean newInstance) {
		if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
			BitbucketPluginLogger.debug(logger, String.format(
					"project displayName=%s, newInstance=",
					project.getDisplayName(), newInstance));
		}
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
		BitbucketPluginLogger.debug(
				logger,
				String.format("project displayName=%s",
						project.getDisplayName()));
		return (BitbucketBuildTrigger) project
				.getTrigger(BitbucketBuildTrigger.class);
	}

	public BitbucketPullRequestsBuilder getBuilder() {
		BitbucketPluginLogger.debug(logger,
				bitbucketPullRequestsBuilder.toString());
		return bitbucketPullRequestsBuilder;
	}

	public QueueTaskFuture<?> startJob(BitbucketCause cause) {
		BitbucketPluginLogger.debug(
				logger,
				String.format("cause shortDescription=%s",
						cause.getShortDescription()));
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

		logger.info(String.format("job=%s, Triggering Build ...",
				this.job.getDisplayName()));

		return this.job.scheduleBuild2(0, cause, new ParametersAction(
				new ArrayList(values.values())));
	}

	@Override
	public void run() {
		if (this.getBuilder().getProject().isDisabled()) {
			logger.info(String
					.format("projectPath=%s, Build Disabled. Skipping ...",
							projectPath));
		} else {
			logger.info(String.format("projectPath=%s, Scheduled ...",
					projectPath));
			this.bitbucketPullRequestsBuilder.run();
		}
		this.getDescriptor().save();
	}

	@Override
	public void stop() {
		logger.info("Stopping ...");
		super.stop();
	}

	public static final class BitbucketBuildTriggerDescriptor extends
			TriggerDescriptor {
		public BitbucketBuildTriggerDescriptor() {
			BitbucketPluginLogger.debug(logger, "INIT");
			load();
		}

		@Override
		public boolean isApplicable(Item item) {
			BitbucketPluginLogger
					.debug(logger,
							String.format("item displayName=%s",
									item.getDisplayName()));
			return true;
		}

		@Override
		public String getDisplayName() {
			BitbucketPluginLogger.debug(logger,
					"Bitbucket Pull Requests Builder");
			return "Bitbucket Pull Requests Builder";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
				BitbucketPluginLogger.debug(
						logger,
						String.format("req url=%s, json=%s",
								req.getRequestURLWithQueryString(),
								json.toString()));
			}
			save();
			return super.configure(req, json);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("BitbucketBuildTrigger [projectPath=")
				.append(projectPath).append(", cron=").append(cron)
				.append(", username=").append(username).append(", password=")
				.append(password).append(", repositoryOwner=")
				.append(repositoryOwner).append(", repositoryName=")
				.append(repositoryName).append(", targetBranch=")
				.append(targetBranch).append(", admins=").append(admins)
				.append(", ciSkipPhrases=").append(ciSkipPhrases).append("]")
				.toString();
	}

}
