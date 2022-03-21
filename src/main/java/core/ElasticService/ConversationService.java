package core.ElasticService;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.Entites.Conversation;
import core.Entites.Tweet;
import core.PerfTracker.PerfStatsTracker;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class ConversationService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    @Autowired
    PerfStatsTracker perfTracker;

    final ObjectMapper objectMapper = new ObjectMapper();


    //to delete old data
    public void deleteUnnecessaryData() {
        long start = System.currentTimeMillis();
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("conversation_sorted");
        deleteByQueryRequest.setQuery(new RangeQueryBuilder("tweetTime").lt(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2881)));
        try {
            restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            perfTracker.put("Elastic:Delete",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        perfTracker.put("Elastic:Delete",System.currentTimeMillis() - start);
    }

    // to save a conversation

    public void saveConversation(Conversation conversation) {
        long start = System.currentTimeMillis();
        elasticsearchOperations.save(conversation);
        perfTracker.put("Elastic:Save",System.currentTimeMillis() - start);
    }


    // get conversation by tweet id, used to set reply

    public Conversation getConversationByTweetId(String id) {
        long start = System.currentTimeMillis();

        try {
            TermQueryBuilder queryBuilder = QueryBuilders.termQuery("conversationId",Long.parseLong(id));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            if(searchHits.length == 1)
            {
                Conversation conversation = objectMapper.readValue(searchHits[0].getSourceAsString(), Conversation.class);
                perfTracker.put("Elastic:Get", System.currentTimeMillis() - start);
                return conversation;
            }
            else if (searchHits.length > 1) {
                Logger.getLogger("DEBUG").info("2 conversation with same id " + id);
                Conversation conversation = objectMapper.readValue(searchHits[0].getSourceAsString(), Conversation.class);
                perfTracker.put("Elastic:Get", System.currentTimeMillis() - start);
                return conversation;
            }
        } catch (IOException e) {
            perfTracker.put("Elastic:Get", System.currentTimeMillis() - start);
            e.printStackTrace();
        }

        perfTracker.put("Elastic:Get", System.currentTimeMillis() - start);
        return null;
    }

    public long getCountInterestingConversation(String mongoQueryId) {
        long start = System.currentTimeMillis();

        try {
            QueryBuilder queryBuilder = QueryBuilders.matchQuery("mongoQueryId",mongoQueryId);
            CountRequest countRequest = new CountRequest("conversation_sorted").query(queryBuilder);
            CountResponse count = restHighLevelClient.count(countRequest,RequestOptions.DEFAULT);
            perfTracker.put("Elastic:Count",System.currentTimeMillis() - start);
            return count.getCount();
        } catch (IOException e) {
            perfTracker.put("Elastic:Count",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return 0;
    }


    // For getting tweets

    public List<Tweet> getInterestingTweets(String mongoQueryId, int docsToGet, long offset) {
        long start = System.currentTimeMillis();

        List<SortBuilder<?>> sortBuilderCollections = Collections.singletonList(SortBuilders.fieldSort("conversationId").order(SortOrder.DESC));
        QueryBuilder queryBuilders = QueryBuilders.termQuery("mongoQueryId",mongoQueryId);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(queryBuilders);
        if(offset > 0)
            boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.rangeQuery("conversationId").lt(offset));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(docsToGet).sort(sortBuilderCollections).query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
        List<Tweet> tweetList = new ArrayList<>();
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            org.elasticsearch.search.SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(org.elasticsearch.search.SearchHit searchHit : searchHits) {
                tweetList.add(objectMapper.readValue(searchHit.getSourceAsString(), Conversation.class).getInterestingTweet());
            }
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            return tweetList;
        } catch (IOException e) {
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return tweetList;
    }

    public List<Tweet> getTweetsByRegex(String regex, int docsToGet, long offset) {
        long start = System.currentTimeMillis();

        List<SortBuilder<?>> sortBuilderCollections = Collections.singletonList(SortBuilders.fieldSort("conversationId").order(SortOrder.DESC));
        RegexpQueryBuilder queryBuilders = QueryBuilders.regexpQuery("interestingTweet.text",regex);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(queryBuilders);
        if(offset > 0)
            boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.rangeQuery("conversationId").lt(offset));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(docsToGet).sort(sortBuilderCollections).query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
        List<Tweet> tweetList = new ArrayList<>();
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(SearchHit searchHit : searchHits) {
                tweetList.add(objectMapper.readValue(searchHit.getSourceAsString(), Conversation.class).getInterestingTweet());
            }
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            return tweetList;
        } catch (IOException e) {
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return tweetList;

    }

    public List<Conversation> getReplies(String parentId) {
        long start = System.currentTimeMillis();
        QueryBuilder queryBuilders = QueryBuilders.termQuery("parentId",parentId);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilders);
        SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
        List<Conversation> conversations = new ArrayList<>();
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            org.elasticsearch.search.SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(org.elasticsearch.search.SearchHit searchHit : searchHits) {
                conversations.add(objectMapper.readValue(searchHit.getSourceAsString(), Conversation.class));
            }
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            return conversations;
        } catch (IOException e) {
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return conversations;
    }

    // For getting conversations

    public List<Conversation> getInterestingConversation(String mongoQueryId, int docsToGet, long offset) {
        long start = System.currentTimeMillis();

        List<SortBuilder<?>> sortBuilderCollections = Collections.singletonList(SortBuilders.fieldSort("conversationId").order(SortOrder.DESC));
        TermQueryBuilder queryBuilders = QueryBuilders.termQuery("mongoQueryId",mongoQueryId);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(queryBuilders);
        if(offset > 0)
            boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.rangeQuery("conversationId").lt(offset));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(docsToGet).sort(sortBuilderCollections).query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
        List<Conversation> conversationList = new ArrayList<>();
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            org.elasticsearch.search.SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(org.elasticsearch.search.SearchHit searchHit : searchHits) {
                Conversation tmp = objectMapper.readValue(searchHit.getSourceAsString(), Conversation.class);
                tmp.setReplies(getReplies(Long.toString(tmp.getConversationId())));
                conversationList.add(tmp);
            }
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            return conversationList;
        } catch (IOException e) {
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return conversationList;
    }


    public List<Conversation> getConversationsByRegex(String regex, int docsToGet, long offset) {
        long start = System.currentTimeMillis();

        List<SortBuilder<?>> sortBuilderCollections = Collections.singletonList(SortBuilders.fieldSort("conversationId").order(SortOrder.DESC));
        RegexpQueryBuilder queryBuilders = QueryBuilders.regexpQuery("interestingTweet.text",regex);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(queryBuilders);
        if(offset > 0)
            boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.rangeQuery("conversationId").lt(offset));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(docsToGet).sort(sortBuilderCollections).query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest("conversation_sorted").source(searchSourceBuilder);
        List<Conversation> conversationList = new ArrayList<>();
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for(SearchHit searchHit : searchHits) {
                conversationList.add(objectMapper.readValue(searchHit.getSourceAsString(), Conversation.class));
            }
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            return conversationList;
        } catch (IOException e) {
            perfTracker.put("Elastic:Get",System.currentTimeMillis() - start);
            e.printStackTrace();
        }
        return conversationList;
    }
}
