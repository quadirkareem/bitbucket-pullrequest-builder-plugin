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
		logger.finer(new StringBuilder("INIT: projectPath=")
				.append(projectPath).append(", cron=").append(cron)
				.append(", username=").append(username).append(", password=")
				.append(password).append(", repositoryOwner=")
				.append(repositoryOwner).append(", repositoryName=")
				.append(repositoryName).append(", targetBranch=")
				.append(targetBranch).append(", admins=").append(admins)
				.append(", ciSkipPhrases=").append(ciSkipPhrases).toString());
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
		logger.finer(projectPath);
		return projectPath;
	}

	public String getCron() {
		logger.finer(cron);
		return cron;
	}

	public String getUsername() {
		logger.finer(username);
		return username;
	}

	public String getPassword() {
		logger.finer(password);
		return password;
	}

	public String getRepositoryOwner() {
		logger.finer(repositoryOwner);
		return repositoryOwner;
	}

	public String getRepositoryName() {
		logger.finer(repositoryName);
		return repositoryName;
	}

	public String getTargetBranch() {
		logger.finer(targetBranch);
		return targetBranch;
	}

	public String getAdmins() {
		logger.finer(admins);
		return admins;
	}

	public Set<String> getAdminsList() {
		logger.finer(Arrays.toString((String[]) adminsList
				.toArray(new String[adminsList.size()])));
		return adminsList;
	}

	public String getCiSkipPhrases() {
		logger.finer(ciSkipPhrases);
		return ciSkipPhrases;
	}

	private void setAdminsList() {
		logger.finer(admins);
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
		logger.finer(new StringBuilder("project displayName=")
				.append(project.getDisplayName()).append(", newInstance=")
				.append(newInstance).toString());
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
		logger.finer("project displayName=" + project.getDisplayName());
		return (BitbucketBuildTrigger) project
				.getTrigger(BitbucketBuildTrigger.class);
	}

	public BitbucketPullRequestsBuilder getBuilder() {
		logger.finer(bitbucketPullRequestsBuilder.toString());
		return bitbucketPullRequestsBuilder;
	}

	public QueueTaskFuture<?> startJob(BitbucketCause cause) {
		logger.finer("BitbucketBuildTrigger.startJob(): cause shortDescription="
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
		if (this.getBuilder().getProject().isDisabled()) {
			logger.info("Build Disabled. Skipping ...");
		} else {
			logger.info("Running ...");
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
			logger.finer("INIT");
			load();
		}

		@Override
		public boolean isApplicable(Item item) {
			logger.finer("item displayName=" + item.getDisplayName());
			return true;
		}

		@Override
		public String getDisplayName() {
			logger.finer("Bitbucket Pull Requests Builder");
			return "Bitbucket Pull Requests Builder";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			logger.finer(new StringBuilder("req url=")
					.append(req.getRequestURLWithQueryString())
					.append(", json=").append(json.toString()).toString());
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
