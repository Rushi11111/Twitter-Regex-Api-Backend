package core.Api;

import core.Entites.Tweet;

import java.util.List;

public class TweetApiResponse {

    private List<Tweet> tweets;
    private long pointer;

    public TweetApiResponse(List<Tweet> tweets) {
        this.tweets = tweets;
        this.pointer = tweets.get(tweets.size()-1).getId();
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    public long getPointer() {
        return pointer;
    }

    public void setPointer(long pointer) {
        this.pointer = pointer;
    }
}
