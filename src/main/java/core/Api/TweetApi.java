package core.Api;

import core.ElasticService.ConversationService;
import core.Entites.Conversation;
import core.Entites.MongoQuery;
import core.Entites.Tweet;
import core.KafkaConsumerService.KafkaConsumerService;
import core.MongoService.MongoQueryDTO;
import core.MongoService.MongoQueryService;
import core.PerfTracker.PerfStatsTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.Math.max;
import static java.lang.Math.min;

@RestController
public class TweetApi {
    @Autowired
    MongoQueryService mongoQueryService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    KafkaConsumerService kafkaConsumerService;

    @Autowired
    PerfStatsTracker perfTracker;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value="/interestingTweet", headers = "Accept=application/json",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody TweetApiResponse
    getInterestingTweets(@RequestBody MongoQueryDTO query) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + query.getTimeout();

        try {
            Pattern.compile(query.getRegex());
        } catch (PatternSyntaxException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Regex Query Invalid");
        }

        List<MongoQuery> mongoQueryList = mongoQueryService.getQueryByRegex(query.getRegex());
        if(mongoQueryList.isEmpty()) {
            MongoQuery mongoQuery = new MongoQuery();
            mongoQuery.setRegex(query.getRegex());
            mongoQuery.setLastQueryTime(System.currentTimeMillis());
            mongoQueryService.createQuery(mongoQuery);

            mongoQueryList = mongoQueryService.getQueryByRegex(query.getRegex());
        } else {
            mongoQueryService.updateLastQueryTime(query.getRegex());
        }

        MongoQuery mongoQuery = mongoQueryList.get(0);

        int numTweetsToGet = query.getDocsToGet();
        conversationService.deleteUnnecessaryData();
        mongoQueryService.deleteUnnecessaryData();

        long presentDocs = conversationService.getCountInterestingConversation(mongoQuery.getId());
        if(presentDocs < numTweetsToGet) {
            kafkaConsumerService.instantRun(mongoQuery.getRegex(),mongoQuery.getId(),numTweetsToGet - presentDocs, max(100000,query.getTimeout()));

            while (presentDocs < numTweetsToGet) {
                try {
                    if(endTime >= System.currentTimeMillis())
                        break;
                    Thread.sleep(max(50,min(200,endTime - System.currentTimeMillis())));
                    presentDocs = conversationService.getCountInterestingConversation(mongoQuery.getId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(presentDocs >= numTweetsToGet) {
                return new TweetApiResponse(conversationService.getInterestingTweets(mongoQuery.getId(), numTweetsToGet, query.getOffset()));
            }
            else {
                List<Tweet> tweetList = conversationService.getTweetsByRegex(mongoQuery.getRegex(), numTweetsToGet, query.getOffset());
                if(tweetList.size() == 0)
                    return new TweetApiResponse(conversationService.getInterestingTweets(mongoQuery.getId(), numTweetsToGet, query.getOffset()));
                return new TweetApiResponse(tweetList);
            }
        } else {
            return new TweetApiResponse(conversationService.getInterestingTweets(mongoQuery.getId(), numTweetsToGet, query.getOffset()));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value="/interestingConversation", headers = "Accept=application/json" ,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody ConversationApiResponse
    getInterestingConversation(@RequestBody MongoQueryDTO query) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + query.getTimeout();

        try {
            Pattern.compile(query.getRegex());
        } catch (PatternSyntaxException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Regex Query Invalid");
        }

        List<MongoQuery> mongoQueryList = mongoQueryService.getQueryByRegex(query.getRegex());
        if(mongoQueryList.isEmpty()) {
            MongoQuery mongoQuery = new MongoQuery();
            mongoQuery.setRegex(query.getRegex());
            mongoQuery.setLastQueryTime(System.currentTimeMillis());
            mongoQueryService.createQuery(mongoQuery);

            mongoQueryList = mongoQueryService.getQueryByRegex(query.getRegex());
        } else {
            mongoQueryService.updateLastQueryTime(query.getRegex());
        }

        MongoQuery mongoQuery = mongoQueryList.get(0);

        int numConversationToGet = query.getDocsToGet();

        conversationService.deleteUnnecessaryData();
        mongoQueryService.deleteUnnecessaryData();

        long presentDocs = conversationService.getCountInterestingConversation(mongoQuery.getId());
        if(presentDocs < numConversationToGet) {
            kafkaConsumerService.instantRun(mongoQuery.getRegex(),mongoQuery.getId(),numConversationToGet - presentDocs, max(100000,query.getTimeout()));

            while (presentDocs < numConversationToGet) {
                try {
                    if(endTime <= System.currentTimeMillis())
                        break;
                    Thread.sleep(max(50,min(200,endTime - System.currentTimeMillis())));
                    presentDocs = conversationService.getCountInterestingConversation(mongoQuery.getId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(presentDocs >= numConversationToGet) {
                return new ConversationApiResponse(conversationService.getInterestingConversation(mongoQuery.getId(), numConversationToGet, query.getOffset()));
            }
            else {
                List<Conversation> conversationList = conversationService.getConversationsByRegex(mongoQuery.getRegex(), numConversationToGet, query.getOffset());
                if(conversationList.size() == 0)
                    return new ConversationApiResponse(conversationService.getInterestingConversation(mongoQuery.getId(), numConversationToGet, query.getOffset()));
                return new ConversationApiResponse(conversationList);
            }
        } else {
            return new ConversationApiResponse(conversationService.getInterestingConversation(mongoQuery.getId(), numConversationToGet, query.getOffset()));
        }
    }

}
