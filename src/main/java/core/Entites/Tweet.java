package core.Entites;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet implements Serializer<Tweet>, Deserializer<Tweet>{
    private static final ObjectMapper tweetObjectMapper = new ObjectMapper();

    static {
        tweetObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Long id;

    @JsonAlias("idStr")
    @JsonProperty("id_str")
    private String idStr;

    private String text;

    private String lang;

    private String source;

    @JsonAlias("parentId")
    @JsonProperty("in_reply_to_status_id")
    private String parentId;

    @JsonAlias("createdAt")
    @JsonProperty("created_at")
    private String createdAt;

    @JsonAlias("extendedEntities")
    @JsonProperty("extended_entities")
    private ExtendedEntities extendedEntities;

    private Tweet retweeted_status;

    @JsonIgnore
    private String fullText;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String screenName;
    @JsonIgnore
    private String profileImgUrl;



    @JsonProperty("user")
    private void unpackNameFromUser(Map<Object,Object> user) {
        name = user.get("name").toString();
        screenName = user.get("screen_name").toString();
        profileImgUrl = user.get("profile_image_url").toString();
    }

    @JsonProperty("extended_tweet")
    private void unpackFullText(Map<Object,Object> extendedTweet) {
        fullText = extendedTweet.get("full_text").toString();
        if(extendedTweet.containsKey("extended_entities")) {
            extendedEntities = tweetObjectMapper.convertValue(extendedTweet.get("extended_entities"),ExtendedEntities.class);
        }
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public ExtendedEntities getExtendedEntities() {
        return extendedEntities;
    }

    public void setExtendedEntities(ExtendedEntities extendedEntities) {
        this.extendedEntities = extendedEntities;
    }

    public Tweet getRetweeted_status() {
        return retweeted_status;
    }

    public void setRetweeted_status(Tweet retweeted_status) {
        this.retweeted_status = retweeted_status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter("screenName")
    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    @JsonGetter("profileImgUrl")
    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public Tweet deserialize(String s, byte[] bytes) {
        Gson gson = new Gson();
        Tweet tweet = null;
        try {
            tweet = gson.fromJson(new String(bytes),Tweet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tweet;
    }

    @Override
    public Tweet deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Tweet tweet) {
        byte[] retVal = null;
        Gson gson = new Gson();
        try {
            retVal = gson.toJson(tweet,Tweet.class).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Tweet data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }


}
