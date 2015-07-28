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

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import antlr.ANTLRException;

/**
 * Created by nishio
 */
public class BitbucketBuildTrigger extends Trigger<AbstractProject<?, ?>> {
    private static final Logger logger = Logger.getLogger(BitbucketBuildTrigger.class.getName());
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("(?s)[,;\\s]\\s*");

    private static final String POST_MERGE_JOB_MSG_NOT_CONFIGURED = "Post Merge Job NOT configured, nothing to do";
    private static final String POST_MERGE_JOB_MSG_NOT_TRIGGERED = "Could NOT trigger Post Merge Job %s,"
        + " Reason: Not found";

    private final String projectPath;
    private final String cron;
    private final String username;
    private final String password;
    private final String repositoryOwner;
    private final String repositoryName;
    private final String targetBranch;
    private final String postMergeJobName;
    private final String admins;
    private final String ciSkipPhrases;
    private Set<String> adminsList;
    private transient BitbucketPullRequestsBuilder bitbucketPullRequestsBuilder;
    private AbstractProject<?, ?> postMergeJob;
    private String postMergeJobMessage = POST_MERGE_JOB_MSG_NOT_CONFIGURED;
    private AbstractProject<?, ?> project;

    @Extension
    public static final BitbucketBuildTriggerDescriptor descriptor = new BitbucketBuildTriggerDescriptor();

    @DataBoundConstructor
    public BitbucketBuildTrigger(String projectPath, String cron, String username, String password,
        String repositoryOwner, String repositoryName, String targetBranch, String postMergeJobName, String admins,
        String ciSkipPhrases) throws ANTLRException {
        super(cron);
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format(
                "Instantiating BitbucketBuildTrigger object: projectPath=%s, cron=%s"
                    + ", username=%s, password=%s, repositoryOwner=%s, repositoryName=%s"
                    + ", targetBranch=%s, postMergeJobName=%s, admins=%s, ciSkipPhrases=%s", projectPath, cron,
                username, password, repositoryOwner, repositoryName, targetBranch, postMergeJobName, admins,
                ciSkipPhrases));
        }
        this.projectPath = projectPath;
        this.cron = cron.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.repositoryOwner = repositoryOwner.trim();
        this.repositoryName = repositoryName.trim();
        this.targetBranch = targetBranch.trim();
        this.postMergeJobName = postMergeJobName.trim();
        this.admins = admins;
        this.ciSkipPhrases = ciSkipPhrases;
        this.setAdminsList();
    }

    public String getProjectPath() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, projectPath);
        return projectPath;
    }

    public String getCron() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, cron);
        return cron;
    }

    public String getUsername() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, username);
        return username;
    }

    public String getPassword() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, password);
        return password;
    }

    public String getRepositoryOwner() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, repositoryOwner);
        return repositoryOwner;
    }

    public String getRepositoryName() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, repositoryName);
        return repositoryName;
    }

    public String getTargetBranch() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, targetBranch);
        return targetBranch;
    }

    public String getPostMergeJobName() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, postMergeJobName);
        return postMergeJobName;
    }

    public AbstractProject<?, ?> getPostMergeJob() {
        if (postMergeJob != null) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, postMergeJob.getFullName());
        }
        else {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, "Post MergeJob is null");
        }
        return postMergeJob;
    }

    public String getPostMergeJobMessage() {
        return postMergeJobMessage;
    }

    public String getAdmins() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, admins);
        return admins;
    }

    public Set<String> getAdminsList() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG,
            Arrays.toString((String[]) adminsList.toArray(new String[adminsList.size()])));
        return adminsList;
    }

    public String getCiSkipPhrases() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, ciSkipPhrases);
        return ciSkipPhrases;
    }

    private void setAdminsList() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, admins);
        if (admins == null || admins.trim().length() == 0) {
            adminsList = new HashSet<String>();
        }
        else {
            String[] users = DELIMITER_PATTERN.split(admins);
            adminsList = new HashSet<String>(users.length);
            for (String u : users) {
                adminsList.add(u.trim().toLowerCase());
            }
        }
    }

    private void setPostMergeJob() {
        if (postMergeJobName != null && postMergeJobName.length() > 0) {
            postMergeJob = Jenkins.getInstance().getItemByFullName(postMergeJobName, AbstractProject.class);
            if (postMergeJob == null) {
                logger.warning(String.format("job=%s - Could NOT find Post Merge Job %s in Jenkins",
                    this.project.getDisplayName(), postMergeJobName));
                postMergeJobMessage = String.format(POST_MERGE_JOB_MSG_NOT_TRIGGERED, postMergeJobName);
            }
            else {
                logger.info(String.format("job=%s - Post Merge Job %s found", this.project.getDisplayName(),
                    postMergeJob.getDisplayName()));
            }
        }
        else {
            logger.info(String.format("job=%s - Post Merge Job is Blank", this.project.getDisplayName()));
        }
    }

    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance) {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG,
                String.format("project displayName=%s, newInstance=", project.getDisplayName(), newInstance));
        }
        try {
            this.project = project;
            this.setPostMergeJob();
            this.bitbucketPullRequestsBuilder = BitbucketPullRequestsBuilder.getBuilder();
            this.bitbucketPullRequestsBuilder.setProject(project);
            this.bitbucketPullRequestsBuilder.setTrigger(this);
            this.bitbucketPullRequestsBuilder.setupBuilder();
        }
        catch (IllegalStateException e) {
            logger.log(Level.SEVERE, "Can't start trigger", e);
            return;
        }
        super.start(project, newInstance);
    }

    public static BitbucketBuildTrigger getTrigger(AbstractProject project) {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("project displayName=%s", project.getDisplayName()));
        return (BitbucketBuildTrigger) project.getTrigger(BitbucketBuildTrigger.class);
    }

    public BitbucketPullRequestsBuilder getBuilder() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, bitbucketPullRequestsBuilder.toString());
        return bitbucketPullRequestsBuilder;
    }

    public QueueTaskFuture<?> startJob(BitbucketCause cause) {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("cause shortDescription=%s", cause.getShortDescription()));
        Map<String, ParameterValue> values = new HashMap<String, ParameterValue>();
        values.put("sourceBranch", new StringParameterValue("sourceBranch", cause.getSourceBranch()));
        values.put("targetBranch", new StringParameterValue("targetBranch", cause.getTargetBranch()));
        values.put("repositoryOwner", new StringParameterValue("repositoryOwner", cause.getRepositoryOwner()));
        values.put("repositonyName", new StringParameterValue("repositoryName", cause.getRepositoryName()));
        values.put("pullRequestId", new StringParameterValue("pullRequestId", cause.getPullRequestId()));
        values.put("destinationRepositoryOwner",
            new StringParameterValue("destinationRepositoryOwner", cause.getDestinationRepositoryOwner()));
        values.put("destinationRepositoryName",
            new StringParameterValue("destinationRepositoryName", cause.getDestinationRepositoryName()));
        values.put("pullRequestTitle", new StringParameterValue("pullRequestTitle", cause.getPullRequestTitle()));

        logger.info(String.format("job=%s, Triggering Build ...", this.job.getDisplayName()));

        return this.job.scheduleBuild2(0, cause, new ParametersAction(new ArrayList(values.values())));
    }

    @Override
    public void run() {
        if (this.getBuilder().getProject().isDisabled()) {
            logger.info(String.format("projectPath=%s, Build Disabled. Skipping ...", projectPath));
        }
        else {
            logger.info(String.format(
                "\n****************\nPull Request Job Triggered: repositoryName=%s, targetBranch=%s, cron=%s\n",
                this.repositoryName, this.targetBranch, this.cron));
            this.bitbucketPullRequestsBuilder.run();
        }
        this.getDescriptor().save();
    }

    @Override
    public void stop() {
        logger.info("\nPull Request Job Stopped.\n****************\n");
        super.stop();
    }

    @Override
    public String toString() {
        return new StringBuilder("BitbucketBuildTrigger [projectPath=").append(projectPath).append(", cron=")
            .append(cron).append(", username=").append(username).append(", password=").append(password)
            .append(", repositoryOwner=").append(repositoryOwner).append(", repositoryName=").append(repositoryName)
            .append(", targetBranch=").append(targetBranch).append(", postMergeJobName=").append(postMergeJobName)
            .append(", admins=").append(admins).append(", ciSkipPhrases=").append(ciSkipPhrases).append("]").toString();
    }

    public static final class BitbucketBuildTriggerDescriptor extends TriggerDescriptor {
        private static final String DISPLAY_NAME = "Bitbucket Pull Request Build Trigger";

        public BitbucketBuildTriggerDescriptor() {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, "INIT");
            load();
        }

        @Override
        public boolean isApplicable(Item item) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("item displayName=%s", item.getDisplayName()));
            return true;
        }

        @Override
        public String getDisplayName() {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, DISPLAY_NAME);
            return DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                logger.log(BitbucketPluginLogger.LEVEL_DEBUG,
                    String.format("req url=%s, json=%s", req.getRequestURLWithQueryString(), json.toString()));
            }
            save();
            return super.configure(req, json);
        }
    }

}
