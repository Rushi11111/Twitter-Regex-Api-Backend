package core.PerfTracker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class PerfStatsConfigs {
    @Bean
    public ConcurrentHashMap<String,Long> storePerf() {
        return new ConcurrentHashMap<>();
    }
}
