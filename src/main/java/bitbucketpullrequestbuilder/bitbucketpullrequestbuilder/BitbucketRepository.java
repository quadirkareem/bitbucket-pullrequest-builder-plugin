package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.BitbucketPullRequest.Operation;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketApiClient;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestComment;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestResponseValue;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketUser;

/**
 * Created by nishio
 */
public class BitbucketRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketRepository.class.getName());
    private static final String BUILD_CMD = "jenkins -t";
    private static final String MERGE_CMD = "jenkins -m";

    private static final String SRC_DEST = "\n\n**Source:** %s **Destination:** %s";
    private static final String REQUESTED_BY = "\n\n**Requested By:** %s";
    private static final String BUILD_START_PREFIX = "## :clock3: Build Started";
    private static final String BUILD_START_PREFIX_LOWER = BUILD_START_PREFIX.toLowerCase();
    private static final String BUILD_START_COMMENT = BUILD_START_PREFIX + REQUESTED_BY + SRC_DEST
        + "\n\n#### *Please wait for build to finish ...*";

    private static final String BUILD_SUCCESS_PREFIX = "## :thumbsup: Build Success";
    private static final String BUILD_SUCCESS_PREFIX_LOWER = BUILD_SUCCESS_PREFIX.toLowerCase();
    private static final String BUILD_FAILURE_PREFIX = "## :thumbsdown: Build Failure";
    private static final String BUILD_FAILURE_PREFIX_LOWER = BUILD_FAILURE_PREFIX.toLowerCase();
    private static final String BUILD_FINISH_SUFFIX = SRC_DEST + "\n\n**URL:** *%s*";

    private static final String MERGE_COMMIT_COMMENT = "Merged in %s (pull request #%s)";

    private static final String MERGE_SUCCESS_PREFIX = "## :lock: Merge Success";
    // private static final String MERGE_SUCCESS_PREFIX_LOWER =
    // MERGE_SUCCESS_PREFIX
    // .toLowerCase();
    private static final String MERGE_SUCCESS_COMMENT = MERGE_SUCCESS_PREFIX + REQUESTED_BY + "\n\n#### *%s*";

    private static final String MERGE_FAILURE_PREFIX = "## :warning: Merge Failure";
    private static final String MERGE_FAILURE_PREFIX_LOWER = MERGE_FAILURE_PREFIX.toLowerCase();
    private static final String MERGE_FAILURE_COMMENT = MERGE_FAILURE_PREFIX + REQUESTED_BY
        + "\n\n#### *Error while trying to merge Pull Request:*" + "\n\n#### *%s*";

    private static final String MERGE_NOT_ALLOWED_PREFIX = "## :warning: Merge Not Allowed";
    private static final String MERGE_NOT_ALLOWED_PREFIX_LOWER = MERGE_NOT_ALLOWED_PREFIX.toLowerCase();
    private static final String MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_PREFIX
        + "\n\n#### *%s does NOT have Merge permissions. Please contact Jenkins Admin for more information.*";

    private static final String SELF_MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_PREFIX
        + "\n\n#### *%s CANNOT Merge his/her own Pull Request. Please request another team member with"
        + " Merge permissions to review and merge.*";

    private static final String POST_MERGE_JOB_MSG_TRIGGERED = "Post Merge Job %s triggered. URL: %s";

    private String projectPath;
    private BitbucketPullRequestsBuilder builder;
    private BitbucketBuildTrigger trigger;
    private BitbucketApiClient client;
    private String buildName;

    public BitbucketRepository(String projectPath, BitbucketPullRequestsBuilder builder) {
        this.projectPath = projectPath;
        this.builder = builder;
        this.buildName = this.builder.getProject().getDisplayName();
        LOG.debug("BitbucketRepository Object Instantiated: projectPath={}, job={}", projectPath, buildName);
    }

    public void init() {
        LOG.info("job={} => init()", buildName);
        trigger = this.builder.getTrigger();
        client = new BitbucketApiClient(trigger.getUsername(), trigger.getPassword(), trigger.getRepositoryOwner(),
            trigger.getRepositoryName());
    }

    public Collection<BitbucketPullRequest> getTargetPullRequests() {
        List<BitbucketPullRequestResponseValue> pullRequests = client.getPullRequests();
        List<BitbucketPullRequest> targetPullRequests = new ArrayList<BitbucketPullRequest>();
        LOG.info("job={} => Total Pull Requests fetched={}", buildName, pullRequests.size());
        if (pullRequests != null && pullRequests.size() > 0) {
            for (BitbucketPullRequestResponseValue pullRequest : pullRequests) {
                filterPullRequest(targetPullRequests, pullRequest);
            }
        }
        LOG.info("job={} => Total Pull Requests post filtering={}", buildName, targetPullRequests.size());
        return targetPullRequests;
    }

    public String postBuildStartCommentTo(BitbucketPullRequestResponseValue pullRequest, BitbucketUser commentAuthor) {
        String id = pullRequest.getId();
        String sourceCommit = pullRequest.getSource().getCommit().getHash();
        String destinationCommit = pullRequest.getDestination().getCommit().getHash();
        LOG.info("job={}, pullRequestId={}, sourceCommit={}, destinationCommit={}", new Object[] { buildName, id,
            sourceCommit, destinationCommit });
        String author = null;
        if (commentAuthor == null) {
            author = "Automatic";
        }
        else {
            author = commentAuthor.toStringFormat();
        }
        String comment = String.format(BUILD_START_COMMENT, author, sourceCommit, destinationCommit);
        BitbucketPullRequestComment commentResponse = this.client.postPullRequestComment(id, comment);
        return commentResponse.getCommentId().toString();
    }

    public void addFutureBuildTasks(Collection<BitbucketPullRequest> pullRequests) {
        LOG.info("job={} => pr_size={}", buildName, pullRequests.size());
        for (BitbucketPullRequest pullRequest : pullRequests) {
            Operation operation = pullRequest.getOperation();
            BitbucketUser commentAuthor = pullRequest.getCommentAuthor();
            String comment = pullRequest.getComment();
            BitbucketPullRequestResponseValue pullRequestValue = pullRequest.getPullRequest();
            switch (operation) {
            case BUILD:
                this.buildPullRequest(pullRequestValue, commentAuthor);
                break;
            case MERGE:
                this.mergePullRequest(pullRequestValue, commentAuthor, comment);
                break;
            default:
                LOG.info("job={} => Nothing to do operation={}, pullRequestId={}", new Object[] { buildName, operation,
                    pullRequestValue.getId() });
            }
        }
    }

    private void buildPullRequest(BitbucketPullRequestResponseValue pullRequestValue, BitbucketUser commentAuthor) {
        LOG.debug("job={} => Posting Build Start Comment for pullRequestId={}", buildName, pullRequestValue.getId());
        String commentId = postBuildStartCommentTo(pullRequestValue, commentAuthor);
        BitbucketCause cause = new BitbucketCause(pullRequestValue.getSource().getBranch().getName(), pullRequestValue
            .getDestination().getBranch().getName(), pullRequestValue.getSource().getRepository().getOwnerName(),
            pullRequestValue.getSource().getRepository().getRepositoryName(), pullRequestValue.getId(),
            pullRequestValue.getDestination().getRepository().getOwnerName(), pullRequestValue.getDestination()
                .getRepository().getRepositoryName(), pullRequestValue.getTitle(), pullRequestValue.getSource()
                .getCommit().getHash(), pullRequestValue.getDestination().getCommit().getHash(), commentId);
        LOG.info("job={} => Starting Build Job for pullRequestId={}", buildName, pullRequestValue.getId());
        this.builder.getTrigger().startJob(cause);
    }

    private void mergePullRequest(BitbucketPullRequestResponseValue pullRequestValue, BitbucketUser commentAuthor,
        String mergeComment) {
        String id = pullRequestValue.getId();
        String sourceBranch = pullRequestValue.getSource().getBranch().getName();
        boolean closeSourceBranch = pullRequestValue.getCloseSourceBranch();
        LOG.info("job={} => Merging Pull Request pullRequestId={}", buildName, id);
        String errorMessage = this.client.mergePullRequest(id, buildMergeComment(id, sourceBranch, mergeComment),
            closeSourceBranch);
        if (errorMessage == null) {
            String triggerResult = this.triggerPostMergeJob(id, commentAuthor);
            this.client.postPullRequestComment(id,
                String.format(MERGE_SUCCESS_COMMENT, commentAuthor.toStringFormat(), triggerResult));
        }
        else {
            this.client.postPullRequestComment(id,
                String.format(MERGE_FAILURE_COMMENT, commentAuthor.toStringFormat(), errorMessage));
        }
    }

    private String triggerPostMergeJob(String id, BitbucketUser commentAuthor) {
        String triggerResult = null;
        AbstractProject<?, ?> postMergeJob = this.trigger.getPostMergeJob();
        if (postMergeJob != null) {
            LOG.debug("job={}, pullRequestId={} - Triggering Post Merge Job {}", new Object[] { buildName, id,
                postMergeJob.getDisplayName() });
            TriggerJobCause cause = new TriggerJobCause(buildName, id, commentAuthor.toString());
            postMergeJob.scheduleBuild2(0, cause);
            String postMergeJobUrl = Jenkins.getInstance().getRootUrl() + postMergeJob.getUrl();
            triggerResult = String.format(POST_MERGE_JOB_MSG_TRIGGERED, postMergeJob.getDisplayName(), postMergeJobUrl);
        }
        else {
            triggerResult = this.trigger.getPostMergeJobMessage();
            LOG.debug("job={}, pullRequestId={} - Post Merge Job is Blank", buildName, id);
        }

        return triggerResult;
    }

    @SuppressWarnings("unused")
    private void deletePullRequestComment(String pullRequestId, String commentId) {
        LOG.info("job={}, pullRequestId={}, commentId={}", new Object[] { buildName, pullRequestId, commentId });
        this.client.deletePullRequestComment(pullRequestId, commentId);
    }

    public void postFinishedComment(String pullRequestId, String sourceCommit, String destinationCommit,
        boolean success, String buildUrl) {
        LOG.info("job={}, pullRequestId={}, sourceCommit={}, destinationCommit={}, success={}, buildUrl={}",
            new Object[] { buildName, pullRequestId, sourceCommit, destinationCommit, success, buildUrl });
        String message = BUILD_FAILURE_PREFIX;
        if (success) {
            message = BUILD_SUCCESS_PREFIX;
        }
        String comment = message
            + String.format(BUILD_FINISH_SUFFIX, sourceCommit, destinationCommit, buildUrl + "console");

        this.client.postPullRequestComment(pullRequestId, comment);
    }

    @Override
    public String toString() {
        return new StringBuilder("BitbucketRepository [projectPath=").append(projectPath).append(", builder=")
            .append(builder).append(", trigger=").append(trigger).append(", client=").append(client).append("]")
            .toString();
    }

    private void filterPullRequest(List<BitbucketPullRequest> targetPullRequests,
        BitbucketPullRequestResponseValue pullRequest) {
        LOG.info("job={} pullRequestId={}", buildName, pullRequest.getId());
        Operation operation = null;
        BitbucketUser commentAuthor = null;
        String mergeComment = null;
        if (pullRequest.getState() != null && pullRequest.getState().equals("OPEN")) {
            String sourceCommit = pullRequest.getSource().getCommit().getHash();
            String destinationCommit = pullRequest.getDestination().getCommit().getHash();
            // branch names are CASE SENSITIVE
            String destinationBranch = pullRequest.getDestination().getBranch().getName();
            if (this.trigger.getTargetBranch().equals(destinationBranch)) {
                String id = pullRequest.getId();

                List<BitbucketPullRequestComment> comments = client.getPullRequestComments(id);

                operation = Operation.BUILD;
                if (comments != null) {
                    boolean mergeMarkerFound = false;
                    boolean successBuildNotFound = true;
                    boolean failedBuildNotFound = true;
                    boolean mergeFailedFound = false;
                    Collections.sort(comments);
                    Collections.reverse(comments);
                    for (BitbucketPullRequestComment comment : comments) {
                        LOG.debug("job={}, pullRequestId={}, comment={}",
                            new Object[] { buildName, id, comment.toString() });
                        String content = comment.getContent();
                        if (content == null || content.isEmpty()) {
                            LOG.debug("job={}, pullRequestId={} - comment is either null or empty", buildName, id);
                            continue;
                        }
                        content = content.toLowerCase().trim();

                        if (!mergeMarkerFound && !mergeFailedFound && content.startsWith(MERGE_CMD)) {
                            LOG.debug("job={}, pullRequestId={} - merge request found", buildName, id);
                            commentAuthor = comment.getAuthor();
                            if (commentAuthor.getUsername().equalsIgnoreCase(pullRequest.getAuthor().getUsername())) {
                                // cannot merge one's own pull request
                                LOG.debug("job={}, pullRequestId={} - pull Request author "
                                    + "[{}] & merge requester [{}] are same, won't merge", new Object[] { buildName,
                                    id, commentAuthor.getUsername(), pullRequest.getAuthor().getUsername() });
                                this.client.postPullRequestComment(id,
                                    String.format(SELF_MERGE_NOT_ALLOWED_COMMENT, commentAuthor.toStringFormat()));

                                operation = null;
                            }
                            else {
                                LOG.debug("job={}, pullRequestId={} - merge found, continue to"
                                    + " find successful build ...", buildName, id);
                                mergeMarkerFound = true;
                                mergeComment = comment.getContent().trim();
                                continue;
                            }
                        }

                        if (mergeMarkerFound) {
                            LOG.debug("job={}, pullRequestId={} - merge request found earlier", buildName, id);

                            // if merge marker found, verify if build finished
                            // successfully for the latest source/destination
                            // commits and that build success comment was added
                            // by Jenkins user
                            if (content.contains(sourceCommit) && content.contains(destinationCommit)
                                && comment.getAuthor().getUsername().equalsIgnoreCase(trigger.getUsername())) {
                                LOG.debug("job={}, pullRequestId={} - this comment contains latest "
                                    + "sourceCommit [{}] and latest destination commit [{}] "
                                    + "and this comment was added by jenkins user", new Object[] { buildName, id,
                                    sourceCommit, destinationCommit });

                                if (content.contains(BUILD_SUCCESS_PREFIX_LOWER)) {
                                    LOG.debug("job={}, pullRequestId={} - this comment contains build "
                                        + "success prefix", buildName, id);
                                    if (this.trigger.getAdminsList()
                                        .contains(commentAuthor.getUsername().toLowerCase())) {
                                        LOG.debug(
                                            "job={}, pullRequestId={} - merge comment author [{}]"
                                                + " is in admins list [{}]",
                                            new Object[] { buildName, id, commentAuthor.getUsername(),
                                                this.trigger.getAdmins() });

                                        operation = Operation.MERGE;
                                    }
                                    else {
                                        LOG.debug("job={}, pullRequestId={} - merge comment author [{}]"
                                            + " is NOT in admins list [{}]", new Object[] { buildName, id,
                                            commentAuthor.getUsername(), this.trigger.getAdmins() });
                                        this.client.postPullRequestComment(id,
                                            String.format(MERGE_NOT_ALLOWED_COMMENT, commentAuthor.toStringFormat()));
                                        operation = null;
                                    }
                                    successBuildNotFound = false;
                                }
                                else if (content.contains(BUILD_FAILURE_PREFIX_LOWER)) {
                                    LOG.debug("job={}, pullRequestId={} - this comment contains build"
                                        + " success prefix", buildName, id);
                                    operation = null;
                                    this.client.postPullRequestComment(id, String.format(MERGE_FAILURE_COMMENT,
                                        commentAuthor.toStringFormat(), "Last Build was not Successful for Source: "
                                            + sourceCommit + " Destination: " + destinationCommit));
                                    failedBuildNotFound = false;
                                }
                                break;
                            }
                        }
                        else if (BUILD_CMD.equals(content)) {
                            LOG.debug("job={}, pullRequestId={} - build request comment found", buildName, id);
                            operation = Operation.BUILD;
                            commentAuthor = comment.getAuthor();
                            break;
                        }
                        else if (comment.getAuthor().getUsername().equalsIgnoreCase(trigger.getUsername())) {
                            LOG.debug("job={}, pullRequestId={} - comment author is jenkins user", buildName, id);
                            if (content.contains(BUILD_START_PREFIX_LOWER)
                                || (content.contains(BUILD_SUCCESS_PREFIX_LOWER) && content.contains(sourceCommit) && content
                                    .contains(destinationCommit))
                                || (content.contains(BUILD_FAILURE_PREFIX_LOWER) && content.contains(sourceCommit) && content
                                    .contains(destinationCommit))) {
                                LOG.debug("job={}, pullRequestId={} - comment contains either "
                                    + "(build started) or (build success and latest source "
                                    + "commit [{}] and latest destination commit [{}]]) "
                                    + "or (build failure and latest source commit and " + "latest destination commit)",
                                    new Object[] { buildName, id, sourceCommit, destinationCommit });
                                operation = null;
                                break;
                            }
                            else if ((content.contains(BUILD_SUCCESS_PREFIX_LOWER) && (!content.contains(sourceCommit) || !content
                                .contains(destinationCommit)))
                                || (content.contains(BUILD_FAILURE_PREFIX_LOWER) && (!content.contains(sourceCommit) && !content
                                    .contains(destinationCommit)))) {
                                LOG.debug("job={}, pullRequestId={} - comment contains either "
                                    + "(build success and either older source commit or "
                                    + "older destination commit) or (build failure and "
                                    + "either older source commit or older destination commit)", buildName, id);
                                operation = Operation.BUILD;
                                break;
                            }
                            else if (content.contains(MERGE_FAILURE_PREFIX_LOWER)
                                || content.contains(MERGE_NOT_ALLOWED_PREFIX_LOWER)) {
                                LOG.debug("job={}, pullRequestId={} - comment contains either"
                                    + " merge failure or merge not allowed", buildName, id);
                                mergeFailedFound = true;
                            }
                        }
                    }
                    if (mergeMarkerFound && successBuildNotFound && failedBuildNotFound && operation != Operation.MERGE) {
                        LOG.debug("job={}, pullRequestId={} - merge request found and build success "
                            + "NOT found and build failure NOT found and operation [{}] is NOT merge", new Object[] {
                            buildName, id, operation });
                        operation = Operation.BUILD;
                        this.client.postPullRequestComment(id, String.format(MERGE_FAILURE_COMMENT,
                            commentAuthor.toStringFormat(), "Could not find Successful Builds for Source: "
                                + sourceCommit + " Destination: " + destinationCommit));
                    }
                }

                LOG.debug("job={}, pullRequestId={}, operation={}", new Object[] { buildName, id, operation });
                if (operation == Operation.BUILD && isSkipBuild(pullRequest.getTitle())) {
                    operation = null;
                }
            }
            else {
                LOG.debug("job={}, target branch [{}] and destinationBranch [{}] do NOT match", new Object[] {
                    buildName, this.trigger.getTargetBranch(), destinationBranch });
            }
        }

        if (operation != null) {
            targetPullRequests.add(new BitbucketPullRequest(operation, pullRequest, commentAuthor, mergeComment));
        }

    }

    private String buildMergeComment(String id, String sourceBranch, String mergeComment) {
        String actualMergeComment = String.format(MERGE_COMMIT_COMMENT, sourceBranch, id);
        if (mergeComment != null && !mergeComment.isEmpty()) {
            mergeComment = mergeComment.substring(MERGE_CMD.length()).trim();
            int startIndex = (mergeComment.indexOf('"') == 0) ? 1 : 0;
            int endIndex = (mergeComment.lastIndexOf('"') == mergeComment.length() - 1) ? (mergeComment.length() - 1)
                : mergeComment.length();
            System.out.println("startIndex=" + startIndex + ", endIndex=" + endIndex);
            if (startIndex > -1 && endIndex > startIndex) {
                actualMergeComment += ". " + mergeComment.substring(startIndex, endIndex);
            }
        }

        return actualMergeComment;
    }

    private boolean isSkipBuild(String pullRequestTitle) {
        LOG.info("job={}, pullRequest Title={}", buildName, pullRequestTitle);
        String skipPhrases = this.trigger.getCiSkipPhrases();
        if (skipPhrases != null && !"".equals(skipPhrases)) {
            String[] phrases = skipPhrases.split(",");
            for (String phrase : phrases) {
                if (pullRequestTitle.toLowerCase().contains(phrase.trim().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

}
