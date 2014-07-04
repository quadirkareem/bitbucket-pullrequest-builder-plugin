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
	public static final String BUILD_REQUEST_MARKER = "jenkins please retest";
	public static final String MERGE_REQUEST_MARKER = "jenkins review complete please merge";
	public static final String DECLINE_REQUEST_MARKER = "jenkins please decline";

	public static final String BUILD_START_MARKER = "[*BuildStarted*] Source: %s Destination: %s \n\n :pensive: Please wait for build to finish";
	public static final String BUILD_FINISH_MARKER = "[*BuildFinished*] Source: %s Destination: %s";
	public static final String BUILD_FINISH_SENTENCE = BUILD_FINISH_MARKER
			+ " \n\n **%s** - %s";
	public static final String BUILD_SUCCESS_COMMENT = ":smiley: SUCCESS";
	public static final String BUILD_FAILURE_COMMENT = ":rage: FAILURE";

	public static final String MERGE_COMMIT_COMMENT = "Merged in %s (pull request #%s)";
	public static final String MERGE_SUCCESS_MARKER = "[*MergeSuccess*]";
	public static final String MERGE_SUCCESS_COMMENT = MERGE_SUCCESS_MARKER
			+ " \n\n :lock: **SUCCESS** - Pull Request Merged on request";
	public static final String MERGE_FAIL_MARKER = "[*MergeFailed*]";
	public static final String MERGE_FAIL_COMMENT = MERGE_FAIL_MARKER
			+ " \n\n :confounded: **FAILURE** - Error while trying to merge Pull Request \n\n %s";
	public static final String MERGE_NOT_ALLOWED_MARKER = "[*MergeNotAllowed*]";
	public static final String MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_MARKER
			+ " \n\n %s does NOT have Merge permissions. Please contact Jenkins Admin for more information.";

	public static final String DECLINE_COMMENT = "[*MergeDeclined*] \n\n :lock: Pull Request Declined on request";
	public static final String DECLINE_NOT_ALLOWED_MARKER = "[*DeclineNotAllowed*]";
	public static final String DECLINE_NOT_ALLOWED_COMMENT = DECLINE_NOT_ALLOWED_MARKER
			+ " \n\n %s does NOT have Decline permissions. Please contact Jenkins Admin for more information.";

	private String projectPath;
	private BitbucketPullRequestsBuilder builder;
	private BitbucketBuildTrigger trigger;
	private BitbucketApiClient client;

	public BitbucketRepository(String projectPath,
			BitbucketPullRequestsBuilder builder) {
		logger.info("INIT: BitbucketRepository(" + projectPath + ", <builder>)");
		this.projectPath = projectPath;
		this.builder = builder;
	}

	public void init() {
		logger.info("BitbucketRepository.init()");
		trigger = this.builder.getTrigger();
		client = new BitbucketApiClient(trigger.getUsername(),
				trigger.getPassword(), trigger.getRepositoryOwner(),
				trigger.getRepositoryName());
	}

	public Collection<BitbucketPullRequest> getTargetPullRequests() {
		logger.info("BitbucketRepository.getTargetPullRequests()");
		logger.info("Fetch PullRequests for Destination Branch: "
				+ this.trigger.getTargetBranch());
		List<BitbucketPullRequestResponseValue> pullRequests = client
				.getPullRequests();
		List<BitbucketPullRequest> targetPullRequests = new ArrayList<BitbucketPullRequest>();
		if (pullRequests != null) {
			for (BitbucketPullRequestResponseValue pullRequest : pullRequests) {
				Operation operation = getPullRequestOperation(pullRequest);
				if (operation != null) {
					targetPullRequests.add(new BitbucketPullRequest(operation,
							pullRequest));
				}
			}
		}
		return targetPullRequests;
	}

	public String postBuildStartCommentTo(
			BitbucketPullRequestResponseValue pullRequest) {
		logger.info("BitbucketRepository.postBuildStartCommentTo(): pullRequestId="
				+ pullRequest.getId());
		String sourceCommit = pullRequest.getSource().getCommit().getHash();
		String destinationCommit = pullRequest.getDestination().getCommit()
				.getHash();
		String comment = String.format(BUILD_START_MARKER, sourceCommit,
				destinationCommit);
		BitbucketPullRequestComment commentResponse = this.client
				.postPullRequestComment(pullRequest.getId(), comment);
		return commentResponse.getCommentId().toString();
	}

	public void addFutureBuildTasks(
			Collection<BitbucketPullRequest> pullRequests) {
		logger.info("BitbucketRepository.addFutureBuildTasks(): pullRequests size="
				+ pullRequests.size());
		for (BitbucketPullRequest pullRequest : pullRequests) {
			Operation operation = pullRequest.getOperation();
			BitbucketPullRequestResponseValue pullRequestValue = pullRequest
					.getPullRequest();
			switch (operation) {
			case BUILD:
				String commentId = postBuildStartCommentTo(pullRequestValue);
				logger.info("BitbucketRepository.addFutureBuildTasks(): pullRequestCommentId="
						+ commentId);
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
				this.mergePullRequest(pullRequestValue);
				break;
			case DECLINE:
				this.declinePullRequest(pullRequestValue);
				break;
			}
		}
	}

	public void declinePullRequest(
			BitbucketPullRequestResponseValue pullRequestValue) {
		String id = pullRequestValue.getId();
		logger.info("BitbucketRepository.declinePullRequest(): pullRequestId="
				+ id);
		this.client.declinePullRequest(id);
		this.client.postPullRequestComment(id, DECLINE_COMMENT);
	}

	public void mergePullRequest(
			BitbucketPullRequestResponseValue pullRequestValue) {
		String id = pullRequestValue.getId();
		String sourceBranch = pullRequestValue.getSource().getBranch()
				.getName();
		logger.info("BitbucketRepository.mergePullRequest(): pullRequestId="
				+ id);
		String errorMessage = this.client.mergePullRequest(id,
				String.format(MERGE_COMMIT_COMMENT, sourceBranch, id));
		if (errorMessage == null) {
			this.client.postPullRequestComment(id, MERGE_SUCCESS_COMMENT);
		} else {
			this.client.postPullRequestComment(id,
					String.format(MERGE_FAIL_COMMENT, errorMessage));
		}
	}

	public void deletePullRequestComment(String pullRequestId, String commentId) {
		logger.info("BitbucketRepository.deletePullRequestComment(): pullRequestId="
				+ pullRequestId + ", commentId=" + commentId);
		this.client.deletePullRequestComment(pullRequestId, commentId);
	}

	public void postFinishedComment(String pullRequestId, String sourceCommit,
			String destinationCommit, boolean success, String buildUrl) {
		logger.info("BitbucketRepository.postFinishedComment(): pullRequestId="
				+ pullRequestId + ", sourceCommit=" + sourceCommit
				+ ", destinationCommit=" + destinationCommit + ", success="
				+ success + ", buildUrl=" + buildUrl);
		String message = BUILD_FAILURE_COMMENT;
		if (success) {
			message = BUILD_SUCCESS_COMMENT;
		}
		String comment = String.format(BUILD_FINISH_SENTENCE, sourceCommit,
				destinationCommit, message, buildUrl);

		this.client.postPullRequestComment(pullRequestId, comment);
	}

	private Operation getPullRequestOperation(
			BitbucketPullRequestResponseValue pullRequest) {
		logger.info("BitbucketRepository.isBuildTarget(): pullRequestId="
				+ pullRequest.getId());
		Operation operation = null;
		if (pullRequest.getState() != null
				&& pullRequest.getState().equals("OPEN")) {
			String sourceCommit = pullRequest.getSource().getCommit().getHash();
			String destinationCommit = pullRequest.getDestination().getCommit()
					.getHash();
			// String owner = destination.getRepository().getOwnerName();
			// String repositoryName = destination.getRepository()
			// .getRepositoryName();
			String destinationBranch = pullRequest.getDestination().getBranch()
					.getName().toLowerCase();
			if (this.trigger.getTargetBranch().contains(destinationBranch)) {
				String id = pullRequest.getId();

				List<BitbucketPullRequestComment> comments = client
						.getPullRequestComments(id);
				String searchStartMarker = String.format(BUILD_START_MARKER,
						sourceCommit, destinationCommit).toLowerCase();
				String searchFinishMarker = String.format(BUILD_FINISH_MARKER,
						sourceCommit, destinationCommit).toLowerCase();

				operation = Operation.BUILD;
				if (comments != null) {
					boolean mergeMarkerFound = false;
					BitbucketUser mergeAuthor = null;
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
							mergeAuthor = comment.getAuthor();
							continue;
						}

						if (mergeMarkerFound) {
							// if merge marker found, verify if build finished
							// successfully
							if (content.contains(BUILD_SUCCESS_COMMENT
									.toLowerCase())) {
								// merge only if merge marker found &&
								// build finished && that too successfully
								if (this.trigger.getAdminsList()
										.contains(
												mergeAuthor.getUsername()
														.toLowerCase())) {
									operation = Operation.MERGE;
								} else {
									String mergeAuthorString = mergeAuthor
											.getDisplayName().concat(" (")
											.concat(mergeAuthor.getUsername())
											.concat(")");
									this.client.postPullRequestComment(id,
											String.format(
													MERGE_NOT_ALLOWED_COMMENT,
													mergeAuthorString));
									operation = null;
								}
								successBuildsNotFound = false;
								break;
							}
						} else if (BUILD_REQUEST_MARKER
								.equalsIgnoreCase(content)) {
							operation = Operation.BUILD;
							break;
						} else if (DECLINE_REQUEST_MARKER
								.equalsIgnoreCase(content)) {
							BitbucketUser declineAuthor = comment.getAuthor();
							if (this.trigger.getAdminsList().contains(
									declineAuthor.getUsername().toLowerCase())) {
								operation = Operation.DECLINE;
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
						} else if (content.contains(searchStartMarker)
								|| content.contains(searchFinishMarker)
								|| content.contains(MERGE_SUCCESS_MARKER
										.toLowerCase())
								|| content.contains(MERGE_FAIL_MARKER
										.toLowerCase())
								|| content.contains(MERGE_NOT_ALLOWED_MARKER
										.toLowerCase())
								|| content.contains(DECLINE_NOT_ALLOWED_MARKER
										.toLowerCase())) {
							operation = null;
							break;
						}
					}
					if (mergeMarkerFound && successBuildsNotFound) {
						operation = null;
						this.client.postPullRequestComment(id, String.format(
								MERGE_FAIL_COMMENT,
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

		return operation;
	}

	private boolean isSkipBuild(String pullRequestTitle) {
		logger.info("BitbucketRepository.isSkipBuild(): pullRequestTitle="
				+ pullRequestTitle);
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
}
