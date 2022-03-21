package core.KafkaConsumerService;

import core.Entites.Conversation;
import core.ElasticService.ConversationService;
import core.Entites.Tweet;
import core.KafkaProducerService.TwitterAndKafkaConfigs;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class KafkaTwitterInstantConsumer implements Runnable{
    @Autowired
    RedisService redisService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    TwitterAndKafkaConfigs twitterAndKafkaConfigs;

    String regex;
    String groupId;
    long numRecords;
    long timeout;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(long numRecords) {
        this.numRecords = numRecords;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public KafkaTwitterInstantConsumer() {}

    public KafkaTwitterInstantConsumer(String regex, String groupId, long numRecords, long timeout) {
        this.regex = regex;
        this.groupId = groupId;
        this.numRecords = numRecords;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        Properties properties=new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, twitterAndKafkaConfigs.getKafkaServer());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Tweet.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        try(KafkaConsumer<String, Tweet> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList("sampled-stream-tweets"));
            DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
            inputFormat.setLenient(true);

            long start = System.currentTimeMillis();
            long end = start + timeout;
            for (int i = 0; i < numRecords; ) {
                ConsumerRecords<String, Tweet> records = consumer.poll(Duration.ofMillis(100));
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

                    if (tweet.getParentId() == null && p.matcher(tweet.getText()).matches()) {
                        boolean saved = false;
                        if(redisService.get(tweet.getIdStr()) == null) {
                            synchronized (tweet.getIdStr()) {
                                if (redisService.get(tweet.getIdStr()) == null) {
                                    redisService.setValue(tweet.getIdStr(), groupId, TimeUnit.MINUTES.toMillis(2880));
                                    saved = true;
                                }
                            }
                        }
                        if(!saved)
                            continue;
                        Conversation conversation = new Conversation();
                        conversation.setMongoQueryId(groupId);
                        conversation.setInterestingTweet(tweet);
                        conversation.setConversationId(tweet.getId());
                        conversation.setId(tweet.getId() + groupId);
                        if(tweet.getCreatedAt() != null) {
                            conversation.setTweetTime(inputFormat.parse(tweet.getCreatedAt()).getTime());
                            conversationService.saveConversation(conversation);
                            i++;
                        }
                    }
                }
                if (System.currentTimeMillis() > end) {
                    Logger.getLogger("TweetsSavedByInstantConsumer").info(Integer.toString(i));
                    break;
                }
            }
        } catch (Exception ignored) {

        }
    }
}

