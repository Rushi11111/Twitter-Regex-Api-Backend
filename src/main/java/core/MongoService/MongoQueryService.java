package core.MongoService;

import core.Entites.MongoQuery;
import core.PerfTracker.PerfStatsTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MongoQueryService {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    PerfStatsTracker perfTracker;

    public void deleteUnnecessaryData() {
        long start = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where("lastQueryTime").lte(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2880)));
        mongoTemplate.findAllAndRemove(query, MongoQuery.class);
        perfTracker.put("Mongo:Delete",System.currentTimeMillis() - start);
    }

    public void createQuery(MongoQuery mongoQuery) {
        long start = System.currentTimeMillis();
        mongoTemplate.insert(mongoQuery);
        perfTracker.put("Mongo:Create",System.currentTimeMillis() - start);
    }

    public List<MongoQuery> getQueryByRegex(String regex) {
        long start = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where("regex").is(regex));
        List<MongoQuery> mongoQueryList = mongoTemplate.find(query,MongoQuery.class);
        perfTracker.put("Mongo:Query",System.currentTimeMillis() - start);
        return mongoQueryList;
    }

    public void updateLastQueryTime(String regex) {
        long start = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where("regex").is(regex));
        Update update = new Update();
        update.set("lastQueryTime", System.currentTimeMillis());
        mongoTemplate.updateMulti(query,update,MongoQuery.class);
        perfTracker.put("Mongo:Update",System.currentTimeMillis() - start);
    }

    public List<MongoQuery> getQuries() {
        // get queries that were last done within 2 minutes of current time
        long start = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where("lastQueryTime").gte(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10000)));
        List<MongoQuery> mongoQueryList = mongoTemplate.find(query,MongoQuery.class);
        perfTracker.put("Mongo:Query",System.currentTimeMillis() - start);
        return mongoQueryList;
    }
}
