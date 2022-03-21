package core.KafkaProducerService;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import core.Entites.Tweet;

import java.lang.reflect.Type;

public class TweetDeserializer implements JsonDeserializer<Tweet> {

    @Override
    public Tweet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Tweet tweet = new Tweet();
        if(!jsonElement.getAsJsonObject().has("text"))
            return tweet;

        if(jsonElement.getAsJsonObject().has("retweeted_status")) {
            jsonElement = jsonElement.getAsJsonObject().get("retweeted_status");
        }

        String text,parentId,createdAt,lang,idStr;
        long id;

        text = jsonElement.getAsJsonObject().get("text").getAsString();
        tweet.setText(text);

        if(jsonElement.getAsJsonObject().has("id")) {
            id = jsonElement.getAsJsonObject().get("id").getAsLong();
            tweet.setId(id);
        }

        if(jsonElement.getAsJsonObject().has("id_str")) {
            idStr = jsonElement.getAsJsonObject().get("id_str").getAsString();
            tweet.setIdStr(idStr);
        }

        if(jsonElement.getAsJsonObject().has("in_reply_to_status_id_str") && !jsonElement.getAsJsonObject().get("in_reply_to_status_id_str").isJsonNull()) {
            parentId = jsonElement.getAsJsonObject().get("in_reply_to_status_id_str").getAsString();
            tweet.setParentId(parentId);
        }


        if(jsonElement.getAsJsonObject().has("created_at")) {
            createdAt = jsonElement.getAsJsonObject().get("created_at").getAsString();
            tweet.setCreatedAt(createdAt);
        }


        if(jsonElement.getAsJsonObject().has("lang")) {
            lang = jsonElement.getAsJsonObject().get("lang").getAsString();
            tweet.setLang(lang);
        }

        return tweet;
    }
}
