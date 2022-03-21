package core.Entites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedEntities {
    @JsonProperty("media")
    private List<TweetMedia> media;

    public List<TweetMedia> getMedia() {
        return media;
    }

    public void setMedia(List<TweetMedia> media) {
        this.media = media;
    }
}
