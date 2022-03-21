

package core.KafkaConsumerService;

import core.Entites.Tweet;
import core.Entites.Conversation;
import core.ElasticService.ConversationService;
import core.Entites.MongoQuery;
import core.KafkaProducerService.TwitterAndKafkaConfigs;
import core.MongoService.MongoQueryService;
import core.RedisService.RedisService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class KafkaTwitterConsumer implements Runnable {
    @Autowired
    RedisService redisService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    MongoQueryService mongoQueryService;

    @Autowired
    TwitterAndKafkaConfigs twitterAndKafkaConfigs;

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KafkaTwitterConsumer() {
    }

    public KafkaTwitterConsumer(String name) {
        this.name = name;
    }

    @Override
    public void run() {

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, twitterAndKafkaConfigs.getKafkaServer());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Tweet.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, name);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, Tweet> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList("sampled-stream-tweets"));
            DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
            inputFormat.setLenient(true);

            for (; ; ) {

                List<MongoQuery> mongoQueryList;
                do {
                    mongoQueryList = mongoQueryService.getQuries();
                    Thread.sleep(1000);
                } while (mongoQueryList.isEmpty());

                ConsumerRecords<String, Tweet> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, Tweet> record : records) {
                    Tweet tweet = record.value();

                    if (tweet.getParentId() != null && redisService.get(tweet.getParentId()) != null) {
                        boolean saved = false;
                        if (redisService.get("Reply:" + tweet.getId()) == null) {
                            synchronized (tweet.getIdStr()) {
                                if (redisService.get("Reply:" + tweet.getId()) == null) {
                                    saved = true;
                                    redisService.setValue("Reply:" + tweet.getId(), tweet.getParentId(), TimeUnit.MINUTES.toMillis(2880));
                                }
                            }
                        }

                        if (!saved)
                            continue;
                        Conversation conversation = new Conversation();
                        conversation.setInterestingTweet(tweet);
                        conversation.setParentId(tweet.getParentId());
                        conversation.setConversationId(Long.parseLong(tweet.getParentId()));
                        conversation.setTweetTime(inputFormat.parse(tweet.getCreatedAt()).getTime());
                        conversation.setId(tweet.getIdStr());
                        conversationService.saveConversation(conversation);
                    }

                    for (MongoQuery mongoQuery : mongoQueryList) {
                        String mongoQueryId = mongoQuery.getId();
                        String regex = mongoQuery.getRegex();
                        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                        boolean saved = false;
                        if (tweet.getParentId() == null && p.matcher(tweet.getText()).matches()) {
                            if (redisService.get(tweet.getIdStr()) == null) {
                                synchronized (tweet.getIdStr()) {
                                    if (redisService.get(tweet.getIdStr()) == null) {
                                        redisService.setValue(tweet.getIdStr(), mongoQueryId, TimeUnit.MINUTES.toMillis(2880));
                                        saved = true;
                                    }
                                }
                            }
                            if (!saved)
                                continue;
                            Conversation conversation = new Conversation();
                            conversation.setMongoQueryId(mongoQueryId);
                            conversation.setInterestingTweet(tweet);
                            conversation.setConversationId(tweet.getId());
                            conversation.setId(tweet.getId() + mongoQueryId);
                            if (tweet.getCreatedAt() != null) {
                                conversation.setTweetTime(inputFormat.parse(tweet.getCreatedAt()).getTime());
                                conversationService.saveConversation(conversation);
                            }

                        }
                    }
                }
            }
        } catch (Exception ignored) {

        }
    }
}

