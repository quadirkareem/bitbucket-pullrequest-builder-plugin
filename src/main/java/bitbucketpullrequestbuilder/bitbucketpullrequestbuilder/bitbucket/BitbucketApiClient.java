package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Created by nishio
 */
public class BitbucketApiClient {
	private static final Logger logger = Logger
			.getLogger(BitbucketApiClient.class.getName());
	private static final String BITBUCKET_HOST = "bitbucket.org";
	private static final String V1_API_BASE_URL = "https://bitbucket.org/api/1.0/repositories/";
	private static final String V2_API_BASE_URL = "https://bitbucket.org/api/2.0/repositories/";
	private final JsonFactory jsonFactory = new JsonFactory();
	private String owner;
	private String repositoryName;
	private Credentials credentials;

	public BitbucketApiClient(String username, String password, String owner,
			String repositoryName) {
		this.credentials = new UsernamePasswordCredentials(username, password);
		this.owner = owner;
		this.repositoryName = repositoryName;
	}

	public List<BitbucketPullRequestResponseValue> getPullRequests() {
		String response = getRequest(V2_API_BASE_URL + this.owner + "/"
				+ this.repositoryName + "/pullrequests/");
		try {
			return parsePullRequestJson(response).getPrValues();
		} catch (Exception e) {
			logger.log(Level.WARNING, "invalid pull request response.", e);
		}
		return null;
	}

	public List<BitbucketPullRequestComment> getPullRequestComments(
			String pullRequestId) {
		// https://api.bitbucket.org/1.0/repositories/ciphercloud/releng/pullrequests/7/comments
		String response = getRequest(V1_API_BASE_URL + this.owner + "/"
				+ this.repositoryName + "/pullrequests/" + pullRequestId
				+ "/comments");
		try {
			return parseCommentJson(response);
		} catch (Exception e) {
			logger.log(Level.WARNING, "invalid pull request response.", e);
		}
		return null;
	}

	public void deletePullRequestComment(String pullRequestId, String commentId) {
		String path = V1_API_BASE_URL + this.owner + "/" + this.repositoryName
				+ "/pullrequests/" + pullRequestId + "/comments/" + commentId;
		// https://bitbucket.org/api/1.0/repositories/{accountname}/{repo_slug}/pullrequests/{pull_request_id}/comments/{comment_id}
		deleteRequest(path);
	}

	public String mergePullRequest(String pullRequestId, String message) {
		// https://bitbucket.org/api/2.0/repositories/{owner}/{repo_slug}/pullrequests/{id}/merge
		String path = V2_API_BASE_URL + this.owner + "/" + this.repositoryName
				+ "/pullrequests/" + pullRequestId + "/merge";
		String errorMessage = null;
		String response = null;
		try {
			NameValuePair content = new NameValuePair("message", message);
			NameValuePair closeSourceBranch = new NameValuePair(
					"close_source_branch", "true");
			response = postRequest(path, new NameValuePair[] { content,
					closeSourceBranch });
			errorMessage = parseMergeResponseJson(response);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return errorMessage;
	}

	public void declinePullRequest(String pullRequestId) {
		// https://bitbucket.org/api/2.0/repositories/{owner}/{repo_slug}/pullrequests/{id}/decline
		String path = V2_API_BASE_URL + this.owner + "/" + this.repositoryName
				+ "/pullrequests/" + pullRequestId + "/decline";
		try {
			postRequest(path, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BitbucketPullRequestComment postPullRequestComment(
			String pullRequestId, String comment) {
		String path = V1_API_BASE_URL + this.owner + "/" + this.repositoryName
				+ "/pullrequests/" + pullRequestId + "/comments";
		try {
			NameValuePair content = new NameValuePair("content", comment);
			String response = postRequest(path, new NameValuePair[] { content });
			return parseSingleCommentJson(response);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getRequest(String path) {
		HttpClient client = new HttpClient();
		client.getState().setCredentials(AuthScope.ANY, credentials);
		GetMethod httpget = new GetMethod(path);
		client.getParams().setAuthenticationPreemptive(true);
		String response = null;
		try {
			client.executeMethod(httpget);
			response = httpget.getResponseBodyAsString();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void deleteRequest(String path) {
		HttpClient client = new HttpClient();
		client.getState().setCredentials(AuthScope.ANY, credentials);
		DeleteMethod httppost = new DeleteMethod(path);
		client.getParams().setAuthenticationPreemptive(true);
		String response = "";
		try {
			client.executeMethod(httppost);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String postRequest(String path, NameValuePair[] params)
			throws UnsupportedEncodingException {
		HttpClient client = new HttpClient();
		client.getState().setCredentials(AuthScope.ANY, credentials);
		PostMethod httppost = new PostMethod(path);
		if (params != null && params.length > 0) {
			httppost.setRequestBody(params);
		}
		client.getParams().setAuthenticationPreemptive(true);
		String response = "";
		try {
			client.executeMethod(httppost);
			response = httppost.getResponseBodyAsString();
			logger.info("API Request Response: " + response);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;

	}

	private BitbucketPullRequestResponse parsePullRequestJson(String response)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		BitbucketPullRequestResponse parsedResponse;
		parsedResponse = mapper.readValue(response,
				BitbucketPullRequestResponse.class);
		return parsedResponse;
	}

	private List<BitbucketPullRequestComment> parseCommentJson(String response)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<BitbucketPullRequestComment> parsedResponse;
		parsedResponse = mapper.readValue(response,
				new TypeReference<List<BitbucketPullRequestComment>>() {
				});
		return parsedResponse;
	}

	private BitbucketPullRequestComment parseSingleCommentJson(String response)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		BitbucketPullRequestComment parsedResponse;
		parsedResponse = mapper.readValue(response,
				BitbucketPullRequestComment.class);
		return parsedResponse;
	}

	private String parseMergeResponseJson(String response) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		BitbucketPullRequestResponseValue parsedResponse;
		parsedResponse = mapper.readValue(response,
				BitbucketPullRequestResponseValue.class);
		String errorMessage = null;
		if (parsedResponse == null || parsedResponse.getId() == null) {
			errorMessage = parseErrorMessageJson(response);
		}
		return errorMessage;
	}

	private String parseErrorMessageJson(String response) throws IOException {
		String errorMessage = null;
		JsonParser parser = jsonFactory.createJsonParser(response);
		ObjectMapper mapper = new ObjectMapper();
		while (parser.nextToken() != null) {
			JsonToken token = parser.nextToken();
			if (token != null) {
				JsonNode root = ((JsonNode) mapper.readTree(parser))
						.path("error");
				errorMessage = root.path("fields").path("newstatus").path(0)
						.asText();
				break;
			}
		}
		return errorMessage;
	}
}
