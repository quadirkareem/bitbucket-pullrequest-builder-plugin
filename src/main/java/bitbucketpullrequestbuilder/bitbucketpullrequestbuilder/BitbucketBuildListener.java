package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nishio
 */
@Extension
public class BitbucketBuildListener extends RunListener<AbstractBuild> {
    private static final Logger LOG = LoggerFactory.getLogger(BitbucketBuildListener.class.getName());

    @Override
    public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {
        LOG.debug("build displayName={}", abstractBuild.getDisplayName());
        // logger.info("BuildListener onStarted called.");
        BitbucketBuildTrigger trigger = BitbucketBuildTrigger.getTrigger(abstractBuild.getProject());
        if (trigger == null) {
            return;
        }
        trigger.getBuilder().getBuilds().onStarted(abstractBuild);
    }

    @Override
    public void onCompleted(AbstractBuild abstractBuild, @Nonnull TaskListener listener) {
        LOG.debug("build displayName={}", abstractBuild.getDisplayName());
        BitbucketBuildTrigger trigger = BitbucketBuildTrigger.getTrigger(abstractBuild.getProject());
        if (trigger == null) {
            return;
        }
        trigger.getBuilder().getBuilds().onCompleted(abstractBuild);
    }
}
