package core.KafkaProducerService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import core.Entites.Tweet;
import core.RedisService.RedisService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class KafkaTwitterProducer implements Runnable{

    @Autowired
    TwitterAndKafkaConfigs twitterAndKafkaConfigs;

    @Autowired
    RedisService redisService;


    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, twitterAndKafkaConfigs.getKafkaServer());
        props.put(ProducerConfig.RETRIES_CONFIG, 2);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 500);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024*1024*256);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Tweet.class.getName());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        KafkaProducer<String, Tweet> producer = new KafkaProducer<>(props);


        BlockingQueue<String> queue = new LinkedBlockingQueue<>(1000000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

        endpoint.trackTerms(Lists.newArrayList("a","is","was","hi","of","for","an","I","am","had","has","have","my","do","done","your","would","should","put"));
        endpoint.addPostParameter("include_entities","false");
        endpoint.addPostParameter("trim_user","true");
        endpoint.addPostParameter("tweet_mode","extended");

        Authentication auth = new OAuth1(twitterAndKafkaConfigs.getConsumerKey(), twitterAndKafkaConfigs.getConsumerSecret(), twitterAndKafkaConfigs.getToken(), twitterAndKafkaConfigs.getSecret());

        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        client.connect();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            while(true) {
                String msg = queue.take();
                Tweet tweet = objectMapper.readValue(msg, Tweet.class);
                if(tweet.getFullText() != null)
                    tweet.setText(tweet.getFullText());
                if(tweet.getRetweeted_status() != null) {
                    tweet = tweet.getRetweeted_status();

                    if(tweet.getFullText() != null)
                        tweet.setText(tweet.getFullText());
                }

                if(tweet.getText() != null && redisService.get("Reply:" + tweet.getIdStr()) == null && redisService.get(tweet.getIdStr()) == null && tweet.getCreatedAt() != null) {
                    producer.send(new ProducerRecord<>("sampled-stream-tweets", tweet.getParentId() == null ? tweet.getIdStr() : tweet.getParentId(), tweet));
                }
            }

        } catch (Exception ignored) {

        } finally {
            producer.flush();
            producer.close();
            client.stop();
        }
    }
}
