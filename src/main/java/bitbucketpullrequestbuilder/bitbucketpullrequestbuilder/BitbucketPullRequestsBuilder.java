package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.AbstractProject;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nishio
 */
public class BitbucketPullRequestsBuilder {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketPullRequestsBuilder.class.getName());
    private AbstractProject<?, ?> project;
    private BitbucketBuildTrigger trigger;
    private BitbucketRepository repository;
    private BitbucketBuilds builds;

    public static BitbucketPullRequestsBuilder getBuilder() {
        logger.debug("==");
        return new BitbucketPullRequestsBuilder();
    }

    public void stop() {
        logger.info("Stopping ...");
    }

    public void run() {
        logger.info("job={} => BitbucketPullRequestsBuilder.run()", project.getDisplayName());
        this.repository.init();
        logger.debug("job={} => getting Target Pull Requests ...", project.getDisplayName());
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
        logger.debug("project displayName={}", project.getDisplayName());
        this.project = project;
    }

    public void setTrigger(BitbucketBuildTrigger trigger) {
        logger.debug("trigger projectPath={}", trigger.getProjectPath());
        this.trigger = trigger;
    }

    public AbstractProject<?, ?> getProject() {
        logger.debug("project displayName={}", this.project.getDisplayName());
        return this.project;
    }

    public BitbucketBuildTrigger getTrigger() {
        logger.debug("==");
        return this.trigger;
    }

    public BitbucketBuilds getBuilds() {
        logger.debug("==");
        return this.builds;
    }
}
