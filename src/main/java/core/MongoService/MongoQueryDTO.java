package core.MongoService;

public class MongoQueryDTO {
    String regex;
    int docsToGet = 10;
    long timeout = 1000;
    long offset = 0;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public int getDocsToGet() {
        return docsToGet;
    }

    public void setDocsToGet(int docsToGet) {
        this.docsToGet = docsToGet;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
