package core.Entites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "conversation_sorted")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversation {
    private String mongoQueryId;
    private Tweet interestingTweet;
    private String parentId;
    private long tweetTime;
    private long conversationId;
    @Transient
    private List<Conversation> replies;
    @Id
    private String id;

    public List<Conversation> getReplies() {
        return replies;
    }

    public void setReplies(List<Conversation> replies) {
        this.replies = replies;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getTweetTime() {
        return tweetTime;
    }

    public void setTweetTime(long tweetTime) {
        this.tweetTime = tweetTime;
    }

    public String getMongoQueryId() {
        return mongoQueryId;
    }

    public void setMongoQueryId(String mongoQueryId) {
        this.mongoQueryId = mongoQueryId;
    }

    public Tweet getInterestingTweet() {
        return interestingTweet;
    }

    public void setInterestingTweet(Tweet interestingTweet) {
        this.interestingTweet = interestingTweet;
    }
}
