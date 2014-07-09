package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.BitbucketPullRequest.Operation;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketApiClient;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestComment;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestResponseValue;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketUser;

/**
 * Created by nishio
 */
public class BitbucketRepository {
	private static final Logger logger = Logger
			.getLogger(BitbucketRepository.class.getName());
	private static final String BUILD_REQUEST_MARKER = "jenkins please retest";
	private static final String MERGE_REQUEST_MARKER = "jenkins review complete please merge";
	private static final String DECLINE_REQUEST_MARKER = "jenkins please decline";

	private static final String SRC_DEST = "\n\n**Source:** %s **Destination:** %s";
	private static final String REQUESTED_BY = "\n\n**Requested By:** %s";
	private static final String BUILD_START_PREFIX = "## :clock3: Build Started";
	private static final String BUILD_START_PREFIX_LOWER = BUILD_START_PREFIX
			.toLowerCase();
	private static final String BUILD_START_COMMENT = BUILD_START_PREFIX
			+ REQUESTED_BY + SRC_DEST
			+ "\n\n#### *Please wait for build to finish ...*";

	private static final String BUILD_SUCCESS_PREFIX = "## :thumbsup: Build Success";
	private static final String BUILD_SUCCESS_PREFIX_LOWER = BUILD_SUCCESS_PREFIX
			.toLowerCase();
	private static final String BUILD_FAILURE_PREFIX = "## :thumbsdown: Build Failure";
	private static final String BUILD_FAILURE_PREFIX_LOWER = BUILD_FAILURE_PREFIX
			.toLowerCase();
	private static final String BUILD_FINISH_SUFFIX = SRC_DEST
			+ "\n\n**URL:** *%s*";

	private static final String MERGE_COMMIT_COMMENT = "Merged in %s (pull request #%s)";

	private static final String MERGE_SUCCESS_PREFIX = "## :lock: Merge Success";
	private static final String MERGE_SUCCESS_PREFIX_LOWER = MERGE_SUCCESS_PREFIX
			.toLowerCase();
	private static final String MERGE_SUCCESS_COMMENT = MERGE_SUCCESS_PREFIX
			+ REQUESTED_BY;

	private static final String MERGE_FAILURE_PREFIX = "## :warning: Merge Failure";
	private static final String MERGE_FAILURE_PREFIX_LOWER = MERGE_FAILURE_PREFIX
			.toLowerCase();
	private static final String MERGE_FAILURE_COMMENT = MERGE_FAILURE_PREFIX
			+ REQUESTED_BY + "\n\nError while trying to merge Pull Request:"
			+ "\n\n%s";

	private static final String MERGE_NOT_ALLOWED_PREFIX = "## :warning: Merge Not Allowed";
	private static final String MERGE_NOT_ALLOWED_PREFIX_LOWER = MERGE_NOT_ALLOWED_PREFIX
			.toLowerCase();
	private static final String MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_PREFIX
			+ REQUESTED_BY
			+ "\n\n#### %s does NOT have Merge permissions. Please contact Jenkins Admin for more information";

	/*
	 * private static final String MERGE_SUCCESS_MARKER = "[*MergeSuccess*]";
	 * private static final String MERGE_SUCCESS_COMMENT = MERGE_SUCCESS_MARKER
	 * + " \n\n :lock: **SUCCESS** - Pull Request Merged on request from %s";
	 * private static final String MERGE_FAIL_MARKER = "[*MergeFailed*]";
	 * private static final String MERGE_FAIL_COMMENT = MERGE_FAIL_MARKER +
	 * " \n\n :confounded: **FAILURE** - Error while trying to merge Pull Request requested by %s \n\n %s"
	 * ; private static final String MERGE_NOT_ALLOWED_MARKER =
	 * "[*MergeNotAllowed*]"; private static final String
	 * MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_MARKER +
	 * " \n\n %s does NOT have Merge permissions. Please contact Jenkins Admin for more information."
	 * ;
	 */

	private static final String DECLINE_PREFIX = "## :x: Declined";
	private static final String DECLINE_PREFIX_LOWER = DECLINE_PREFIX
			.toLowerCase();
	private static final String DECLINE_COMMENT = DECLINE_PREFIX + REQUESTED_BY;

	private static final String DECLINE_NOT_ALLOWED_PREFIX = "## :warning: Decline Not Allowed";
	private static final String DECLINE_NOT_ALLOWED_PREFIX_LOWER = DECLINE_NOT_ALLOWED_PREFIX
			.toLowerCase();
	private static final String DECLINE_NOT_ALLOWED_COMMENT = DECLINE_NOT_ALLOWED_PREFIX
			+ REQUESTED_BY
			+ "\n\n#### %s does NOT have Decline permissions. Please contact Jenkins Admin for more information";

	private String projectPath;
	private BitbucketPullRequestsBuilder builder;
	private BitbucketBuildTrigger trigger;
	private BitbucketApiClient client;

	public BitbucketRepository(String projectPath,
			BitbucketPullRequestsBuilder builder) {
		logger.finer(new StringBuilder("INIT").append(", projectPath=")
				.append(projectPath).append(", <builder>)").toString());
		this.projectPath = projectPath;
		this.builder = builder;
	}

	public void init() {
		logger.info("Initializing ...");
		trigger = this.builder.getTrigger();
		client = new BitbucketApiClient(trigger.getUsername(),
				trigger.getPassword(), trigger.getRepositoryOwner(),
				trigger.getRepositoryName());
	}

	public Collection<BitbucketPullRequest> getTargetPullRequests() {
		List<BitbucketPullRequestResponseValue> pullRequests = client
				.getPullRequests();
		List<BitbucketPullRequest> targetPullRequests = new ArrayList<BitbucketPullRequest>();
		logger.info("Fetched Pull Requests from Bitbucket: "
				+ pullRequests.size());
		if (pullRequests != null && pullRequests.size() > 0) {
			for (BitbucketPullRequestResponseValue pullRequest : pullRequests) {
				filterPullRequest(targetPullRequests, pullRequest);
			}
		}
		logger.info("Filtered Pull Requests: " + targetPullRequests.size());
		return targetPullRequests;
	}

	public String postBuildStartCommentTo(
			BitbucketPullRequestResponseValue pullRequest,
			BitbucketUser commentAuthor) {
		String id = pullRequest.getId();
		String sourceCommit = pullRequest.getSource().getCommit().getHash();
		String destinationCommit = pullRequest.getDestination().getCommit()
				.getHash();
		logger.info(new StringBuilder("pullRequest id=").append(id)
				.append(", sourceCommit=").append(sourceCommit)
				.append(", destinationCommit=").append(destinationCommit)
				.toString());
		String author = null;
		if (commentAuthor == null) {
			author = "Automatic";
		} else {
			author = commentAuthor.toStringFormat();
		}
		String comment = String.format(BUILD_START_COMMENT, author,
				sourceCommit, destinationCommit);
		BitbucketPullRequestComment commentResponse = this.client
				.postPullRequestComment(id, comment);
		return commentResponse.getCommentId().toString();
	}

	public void addFutureBuildTasks(
			Collection<BitbucketPullRequest> pullRequests) {
		logger.info("pullRequests size=" + pullRequests.size());
		for (BitbucketPullRequest pullRequest : pullRequests) {
			Operation operation = pullRequest.getOperation();
			BitbucketUser commentAuthor = pullRequest.getCommentAuthor();
			BitbucketPullRequestResponseValue pullRequestValue = pullRequest
					.getPullRequest();
			switch (operation) {
			case BUILD:
				String commentId = postBuildStartCommentTo(pullRequestValue,
						commentAuthor);
				logger.info("pullRequestCommentId=" + commentId);
				BitbucketCause cause = new BitbucketCause(
						pullRequestValue.getSource().getBranch().getName(),
						pullRequestValue.getDestination().getBranch().getName(),
						pullRequestValue.getSource().getRepository()
								.getOwnerName(),
						pullRequestValue.getSource().getRepository()
								.getRepositoryName(),
						pullRequestValue.getId(),
						pullRequestValue.getDestination().getRepository()
								.getOwnerName(),
						pullRequestValue.getDestination().getRepository()
								.getRepositoryName(),
						pullRequestValue.getTitle(),
						pullRequestValue.getSource().getCommit().getHash(),
						pullRequestValue.getDestination().getCommit().getHash(),
						commentId);
				this.builder.getTrigger().startJob(cause);
				break;
			case MERGE:
				this.mergePullRequest(pullRequestValue, commentAuthor);
				break;
			case DECLINE:
				this.declinePullRequest(pullRequestValue, commentAuthor);
				break;
			}
		}
	}

	public void declinePullRequest(
			BitbucketPullRequestResponseValue pullRequestValue,
			BitbucketUser commentAuthor) {
		String id = pullRequestValue.getId();
		logger.info("pullRequest id=" + id);
		this.client.declinePullRequest(id);
		this.client.postPullRequestComment(id,
				String.format(DECLINE_COMMENT, commentAuthor.toStringFormat()));
	}

	public void mergePullRequest(
			BitbucketPullRequestResponseValue pullRequestValue,
			BitbucketUser commentAuthor) {
		String id = pullRequestValue.getId();
		String sourceBranch = pullRequestValue.getSource().getBranch()
				.getName();
		logger.info(new StringBuilder("pullRequestId=").append(id)
				.append(", sourceBranch=").append(sourceBranch).toString());
		String errorMessage = this.client.mergePullRequest(id,
				String.format(MERGE_COMMIT_COMMENT, sourceBranch, id));
		if (errorMessage == null) {
			this.client.postPullRequestComment(
					id,
					String.format(MERGE_SUCCESS_COMMENT,
							commentAuthor.toStringFormat()));
		} else {
			this.client.postPullRequestComment(
					id,
					String.format(MERGE_FAILURE_COMMENT,
							commentAuthor.toStringFormat(), errorMessage));
		}
	}

	public void deletePullRequestComment(String pullRequestId, String commentId) {
		logger.info(new StringBuilder("pullRequestId=").append(pullRequestId)
				.append(", commentId=").append(commentId).toString());
		this.client.deletePullRequestComment(pullRequestId, commentId);
	}

	public void postFinishedComment(String pullRequestId, String sourceCommit,
			String destinationCommit, boolean success, String buildUrl) {
		logger.info(new StringBuilder("pullRequestId=").append(pullRequestId)
				.append(", sourceCommit=").append(sourceCommit)
				.append(", destinationCommit=").append(destinationCommit)
				.append(", success=").append(success).append(", buildUrl=")
				.append(buildUrl).toString());
		String message = BUILD_FAILURE_PREFIX;
		if (success) {
			message = BUILD_SUCCESS_PREFIX;
		}
		String comment = message
				+ String.format(BUILD_FINISH_SUFFIX, sourceCommit,
						destinationCommit, buildUrl + "console");

		this.client.postPullRequestComment(pullRequestId, comment);
	}

	private void filterPullRequest(
			List<BitbucketPullRequest> targetPullRequests,
			BitbucketPullRequestResponseValue pullRequest) {
		logger.info("pullRequest id=" + pullRequest.getId());
		Operation operation = null;
		BitbucketUser commentAuthor = null;
		if (pullRequest.getState() != null
				&& pullRequest.getState().equals("OPEN")) {
			String sourceCommit = pullRequest.getSource().getCommit().getHash();
			String destinationCommit = pullRequest.getDestination().getCommit()
					.getHash();
			// branch names are CASE SENSITIVE
			String destinationBranch = pullRequest.getDestination().getBranch()
					.getName();
			if (this.trigger.getTargetBranch().contains(destinationBranch)) {
				String id = pullRequest.getId();

				List<BitbucketPullRequestComment> comments = client
						.getPullRequestComments(id);
				String commitMarker = String.format(SRC_DEST, sourceCommit,
						destinationCommit).toLowerCase();

				operation = Operation.BUILD;
				if (comments != null) {
					boolean mergeMarkerFound = false;
					boolean successBuildsNotFound = true;
					Collections.sort(comments);
					Collections.reverse(comments);
					for (BitbucketPullRequestComment comment : comments) {
						logger.info("Comment=" + comment);
						String content = comment.getContent();
						if (content == null || content.isEmpty()) {
							continue;
						}
						content = content.toLowerCase().trim();

						if (!mergeMarkerFound
								&& MERGE_REQUEST_MARKER
										.equalsIgnoreCase(content)) {
							mergeMarkerFound = true;
							commentAuthor = comment.getAuthor();
							continue;
						}

						if (mergeMarkerFound) {
							// if merge marker found, verify if build finished
							// successfully for the latest source/destination
							// commits and that build success comment was added
							// by Jenkins user
							if (content.contains(BUILD_SUCCESS_PREFIX_LOWER)
									&& content.contains(commitMarker) && comment.getAuthor().getUsername()
									.equalsIgnoreCase(trigger.getUsername())) {
								if (this.trigger.getAdminsList().contains(
										commentAuthor.getUsername()
												.toLowerCase())) {
									operation = Operation.MERGE;
								} else {
									this.client.postPullRequestComment(id,
											String.format(
													MERGE_NOT_ALLOWED_COMMENT,
													commentAuthor
															.toStringFormat()));
									operation = null;
								}
								successBuildsNotFound = false;
								break;
							}
						} else if (BUILD_REQUEST_MARKER
								.equalsIgnoreCase(content)) {
							operation = Operation.BUILD;
							commentAuthor = comment.getAuthor();
							break;
						} else if (DECLINE_REQUEST_MARKER
								.equalsIgnoreCase(content)) {
							BitbucketUser declineAuthor = comment.getAuthor();
							if (this.trigger.getAdminsList().contains(
									declineAuthor.getUsername().toLowerCase())) {
								operation = Operation.DECLINE;
								commentAuthor = comment.getAuthor();
							} else {
								String declineAuthorString = declineAuthor
										.getDisplayName().concat(" (")
										.concat(declineAuthor.getUsername())
										.concat(")");
								this.client.postPullRequestComment(id, String
										.format(DECLINE_NOT_ALLOWED_COMMENT,
												declineAuthorString));
								operation = null;
							}
							break;
						} else if (comment.getAuthor().getUsername()
								.equalsIgnoreCase(trigger.getUsername())
								&& (content.contains(BUILD_START_PREFIX_LOWER)
										|| content
												.contains(BUILD_SUCCESS_PREFIX_LOWER)
										|| content
												.contains(BUILD_FAILURE_PREFIX_LOWER)
										|| content
												.contains(MERGE_SUCCESS_PREFIX_LOWER)
										|| content
												.contains(MERGE_FAILURE_PREFIX_LOWER)
										|| content
												.contains(MERGE_NOT_ALLOWED_PREFIX_LOWER)
										|| content
												.contains(DECLINE_PREFIX_LOWER) || content
											.contains(DECLINE_NOT_ALLOWED_PREFIX_LOWER))) {
							operation = null;
							break;
						}
					}
					if (mergeMarkerFound && successBuildsNotFound) {
						operation = null;
						this.client.postPullRequestComment(id, String.format(
								MERGE_FAILURE_COMMENT,
								commentAuthor.toStringFormat(),
								"Could not find Successful Builds for Source: "
										+ sourceCommit + " Destination: "
										+ destinationCommit));
					}
				}

				if (operation == Operation.BUILD
						&& isSkipBuild(pullRequest.getTitle())) {
					operation = null;
				}
			}
		}

		if (operation != null) {
			targetPullRequests.add(new BitbucketPullRequest(operation,
					pullRequest, commentAuthor));
		}

	}

	private boolean isSkipBuild(String pullRequestTitle) {
		logger.info("pullRequest Title=" + pullRequestTitle);
		String skipPhrases = this.trigger.getCiSkipPhrases();
		if (skipPhrases != null && !"".equals(skipPhrases)) {
			String[] phrases = skipPhrases.split(",");
			for (String phrase : phrases) {
				if (pullRequestTitle.toLowerCase().contains(
						phrase.trim().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return new StringBuilder("BitbucketRepository [projectPath=")
				.append(projectPath).append(", builder=").append(builder)
				.append(", trigger=").append(trigger).append(", client=")
				.append(client).append("]").toString();
	}

}
