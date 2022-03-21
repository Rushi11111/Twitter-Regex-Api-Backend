package core.MongoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfigs {
    @Value("${mongo.connectionString}")
    private String mongoConnectionString;

    @Bean
    public MongoTemplate getMongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoConnectionString));
    }
}
