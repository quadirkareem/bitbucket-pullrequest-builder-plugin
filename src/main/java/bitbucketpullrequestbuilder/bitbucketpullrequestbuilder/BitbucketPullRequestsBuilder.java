package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by nishio
 */
public class BitbucketPullRequestsBuilder {
    private static final Logger logger = Logger.getLogger(BitbucketPullRequestsBuilder.class.getName());
    private AbstractProject<?, ?> project;
    private BitbucketBuildTrigger trigger;
    private BitbucketRepository repository;
    private BitbucketBuilds builds;

    public static BitbucketPullRequestsBuilder getBuilder() {
        logger.log(BitbucketPluginLogger.LEVEL_DEBUG, "INIT");
        return new BitbucketPullRequestsBuilder();
    }

    public void stop() {
        logger.info("Stopping ...");
    }

    public void run() {
        logger.info(String.format("job=%s => BitbucketPullRequestsBuilder.run()", project.getDisplayName()));
        this.repository.init();
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG,
                String.format("job=%s => getting Target Pull Requests ...", project.getDisplayName()));
        }
        Collection<BitbucketPullRequest> targetPullRequests = this.repository.getTargetPullRequests();
        this.repository.addFutureBuildTasks(targetPullRequests);
    }

    public BitbucketPullRequestsBuilder setupBuilder() {
        logger.info("Setting up Builder ...");
        if (this.project == null || this.trigger == null) {
            throw new IllegalStateException();
        }
        this.repository = new BitbucketRepository(this.trigger.getProjectPath(), this);
        this.builds = new BitbucketBuilds(this.trigger, this.repository);
        return this;
    }

    public void setProject(AbstractProject<?, ?> project) {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("project displayName=%s", project.getDisplayName()));
        }
        this.project = project;
    }

    public void setTrigger(BitbucketBuildTrigger trigger) {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("trigger projectPath=%s", trigger.getProjectPath()));
        }
        this.trigger = trigger;
    }

    public AbstractProject<?, ?> getProject() {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, String.format("project displayName=%s", this.project.getDisplayName()));
        }
        return this.project;
    }

    public BitbucketBuildTrigger getTrigger() {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, "==");
        }
        return this.trigger;
    }

    public BitbucketBuilds getBuilds() {
        if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
            logger.log(BitbucketPluginLogger.LEVEL_DEBUG, "==");
        }
        return this.builds;
    }
}
