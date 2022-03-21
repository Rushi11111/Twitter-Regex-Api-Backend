package core.KafkaProducerService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitterAndKafkaConfigs {
    @Value("${twitter.bearertoken}")
    private String BEARER_TOKEN;
    @Value("${twitter.consumerKey}")
    private String consumerKey;
    @Value("${twitter.consumerSecret}")
    private String consumerSecret;
    @Value("${twitter.token}")
    private String token;
    @Value("${twitter.secret}")
    private String secret;

    @Value("${kafka.server}")
    private String kafkaServer;

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getBEARER_TOKEN() {
        return BEARER_TOKEN;
    }

    public void setBEARER_TOKEN(String BEARER_TOKEN) {
        this.BEARER_TOKEN = BEARER_TOKEN;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
