package core.Entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoQuery {

    @Id
    private String id;
    @Indexed
    private String regex;
    @Indexed
    private long lastQueryTime;

    public long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public String getId() {
        return id;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
