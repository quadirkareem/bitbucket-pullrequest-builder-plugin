package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by nishio
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestResponse {
    private int pageLength;
    private List<BitbucketPullRequestResponseValue> prValues;
    private int page;
    private int size;

    @JsonProperty("pagelen")
    public int getPageLength() {
        return pageLength;
    }

    @JsonProperty("pagelen")
    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    @JsonProperty("values")
    public List<BitbucketPullRequestResponseValue> getPrValues() {
        return prValues;
    }

    @JsonProperty("values")
    public void setPrValues(List<BitbucketPullRequestResponseValue> prValues) {
        this.prValues = prValues;
    }

    @JsonProperty("page")
    public int getPage() {
        return page;
    }

    @JsonProperty("page")
    public void setPage(int page) {
        this.page = page;
    }

    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("pageLength=").append(pageLength).append(", prValues=")
            .append(Arrays.toString(prValues.toArray(new BitbucketPullRequestResponseValue[prValues.size()])))
            .append(", page=").append(page).append("size=").append(size).toString();
    }
}
