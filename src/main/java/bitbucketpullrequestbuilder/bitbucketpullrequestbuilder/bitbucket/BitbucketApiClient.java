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

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.BitbucketPluginLogger;

/**
 * Created by nishio
 */
public class BitbucketApiClient {
    private static final Logger logger = Logger.getLogger(BitbucketApiClient.class.getName());
    private static final String BITBUCKET_HOST = "bitbucket.org";
    private static final String V1_API_BASE_URL = "https://bitbucket.org/api/1.0/repositories/";
    private static final String V2_API_BASE_URL = "https://bitbucket.org/api/2.0/repositories/";
    private final JsonFactory jsonFactory = new JsonFactory();
    private String owner;
    private String repositoryName;
    private Credentials credentials;
    private String pullRequestsUrl;

    public BitbucketApiClient(String username, String password, String owner, String repositoryName) {
        this.credentials = new UsernamePasswordCredentials(username, password);
        this.owner = owner;
        this.repositoryName = repositoryName;
        this.pullRequestsUrl = V2_API_BASE_URL + this.owner + "/" + this.repositoryName + "/pullrequests/";
    }

    public List<BitbucketPullRequestResponseValue> getPullRequests() {
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger, String.format("Invoking Http Request %s", pullRequestsUrl));
            }
            String response = getRequest(pullRequestsUrl);
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger, String.format("Response for %s:\n%s", pullRequestsUrl, response));
            }
            return parsePullRequestJson(response).getPrValues();
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Error while getting Pull Requests or parsing response", e);
        }
        return null;
    }

    public List<BitbucketPullRequestComment> getPullRequestComments(String pullRequestId) {
        String pullRequestCommentsUrl = getRequest(V1_API_BASE_URL + this.owner + "/" + this.repositoryName
            + "/pullrequests/" + pullRequestId + "/comments");
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger, String.format("Invoking Http Request %s", pullRequestCommentsUrl));
            }
            String response = getRequest(pullRequestCommentsUrl);
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger,
                    String.format("Response for %s:\n%s", pullRequestCommentsUrl, response));
            }
            return parseCommentJson(response);
        }
        catch (Exception e) {
            logger
                .log(Level.WARNING, String
                    .format("Error while getting Pull Request Comments or parsing response for pullRequestId=%s",
                        pullRequestId), e);
        }
        return null;
    }

    public void deletePullRequestComment(String pullRequestId, String commentId) {
        // https://bitbucket.org/api/1.0/repositories/{accountname}/{repo_slug}/pullrequests/{pull_request_id}/comments/{comment_id}
        String deletePullRequestCommentUrl = V1_API_BASE_URL + this.owner + "/" + this.repositoryName
            + "/pullrequests/" + pullRequestId + "/comments/" + commentId;
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger,
                    String.format("Invoking Http Request %s", deletePullRequestCommentUrl));
            }
            deleteRequest(deletePullRequestCommentUrl);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, String.format(
                "Error while deleting Pull Request comment for pullRequestId=%s, commentId=%s", pullRequestId,
                commentId), e);
        }
    }

    public String mergePullRequest(String pullRequestId, String message, boolean closeSourceBranch) {
        // https://bitbucket.org/api/2.0/repositories/{owner}/{repo_slug}/pullrequests/{id}/merge
        String mergePullRequestUrl = V2_API_BASE_URL + this.owner + "/" + this.repositoryName + "/pullrequests/"
            + pullRequestId + "/merge";
        String errorMessage = null;
        String response = null;
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger, String.format("Invoking Http Request %s", mergePullRequestUrl));
            }
            NameValuePair content = new NameValuePair("message", message);
            NameValuePair closeSourceBranchNVP = new NameValuePair("close_source_branch",
                Boolean.toString(closeSourceBranch));
            response = postRequest(mergePullRequestUrl, new NameValuePair[] { content, closeSourceBranchNVP });
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger,
                    String.format("Response for %s:\n%s", mergePullRequestUrl, response));
            }
            errorMessage = parseMergeResponseJson(response);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            logger.log(Level.WARNING,
                String.format("Error while Merging Pull Request or parsing response pullRequestId=%s", pullRequestId),
                e);
        }

        return errorMessage;
    }

    public void declinePullRequest(String pullRequestId) {
        // https://bitbucket.org/api/2.0/repositories/{owner}/{repo_slug}/pullrequests/{id}/decline
        String declinePullRequestUrl = V2_API_BASE_URL + this.owner + "/" + this.repositoryName + "/pullrequests/"
            + pullRequestId + "/decline";
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger, String.format("Invoking Http Request %s", declinePullRequestUrl));
            }
            postRequest(declinePullRequestUrl, null);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            logger.log(Level.WARNING,
                String.format("Error while Declining Pull Request pullRequestId=%s", pullRequestId), e);
        }
    }

    public BitbucketPullRequestComment postPullRequestComment(String pullRequestId, String comment) {
        String postPullRequestCommentUrl = V1_API_BASE_URL + this.owner + "/" + this.repositoryName + "/pullrequests/"
            + pullRequestId + "/comments";
        try {
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger,
                    String.format("Invoking Http Request %s", postPullRequestCommentUrl));
            }
            NameValuePair content = new NameValuePair("content", comment);
            String response = postRequest(postPullRequestCommentUrl, new NameValuePair[] { content });
            if (logger.isLoggable(BitbucketPluginLogger.LEVEL_DEBUG)) {
                BitbucketPluginLogger.debug(logger,
                    String.format("Response for %s:\n%s", postPullRequestCommentUrl, response));
            }
            return parseSingleCommentJson(response);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, String.format(
                "Error while PostingPull Request Comment or parsing response pullRequestId=%s, comment=%s",
                pullRequestId, comment), e);
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
        }
        catch (HttpException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void deleteRequest(String path) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, credentials);
        DeleteMethod httppost = new DeleteMethod(path);
        client.getParams().setAuthenticationPreemptive(true);
        String response = "";
        try {
            client.executeMethod(httppost);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String postRequest(String path, NameValuePair[] params) throws UnsupportedEncodingException {
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
            logger.finer("API Request Response: " + response);
        }
        catch (HttpException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }

    private BitbucketPullRequestResponse parsePullRequestJson(String response) throws IOException {
        BitbucketPluginLogger.debug(logger, "parsePullRequestJson():\n" + response);
        ObjectMapper mapper = new ObjectMapper();
        BitbucketPullRequestResponse parsedResponse;
        parsedResponse = mapper.readValue(response, BitbucketPullRequestResponse.class);
        return parsedResponse;
    }

    private List<BitbucketPullRequestComment> parseCommentJson(String response) throws IOException {
        BitbucketPluginLogger.debug(logger, "parseCommentJson():\n" + response);
        ObjectMapper mapper = new ObjectMapper();
        List<BitbucketPullRequestComment> parsedResponse;
        parsedResponse = mapper.readValue(response, new TypeReference<List<BitbucketPullRequestComment>>() {});
        return parsedResponse;
    }

    private BitbucketPullRequestComment parseSingleCommentJson(String response) throws IOException {
        BitbucketPluginLogger.debug(logger, "parseSingleCommentJson():\n" + response);
        ObjectMapper mapper = new ObjectMapper();
        BitbucketPullRequestComment parsedResponse;
        parsedResponse = mapper.readValue(response, BitbucketPullRequestComment.class);
        return parsedResponse;
    }

    private String parseMergeResponseJson(String response) throws IOException {
        BitbucketPluginLogger.debug(logger, "parseMergeResponseJson():\n" + response);
        ObjectMapper mapper = new ObjectMapper();
        BitbucketPullRequestResponseValue parsedResponse;
        parsedResponse = mapper.readValue(response, BitbucketPullRequestResponseValue.class);
        String errorMessage = null;
        if (parsedResponse == null || parsedResponse.getId() == null) {
            errorMessage = parseErrorMessageJson(response);
        }
        return errorMessage;
    }

    private String parseErrorMessageJson(String response) throws IOException {
        BitbucketPluginLogger.debug(logger, "parseErrorMessageJson():\n" + response);
        String errorMessage = null;
        JsonParser parser = jsonFactory.createJsonParser(response);
        ObjectMapper mapper = new ObjectMapper();
        while (parser.nextToken() != null) {
            JsonToken token = parser.nextToken();
            if (token != null) {
                JsonNode root = ((JsonNode) mapper.readTree(parser)).path("error");
                errorMessage = root.path("fields").path("newstatus").path(0).asText().trim();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = root.path("message").asText().trim();
                }
                break;
            }
        }
        return errorMessage;
    }

}
