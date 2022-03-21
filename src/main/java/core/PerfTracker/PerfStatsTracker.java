package core.PerfTracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Scope("singleton")
public class PerfStatsTracker {

    @Autowired
    private Map<String,Long> storePerf;

    public void put(String key, Long value) {
        storePerf.putIfAbsent(key, 0L);
        storePerf.put(key, storePerf.get(key) + value);
    }

    public Long get(String key) {
        return storePerf.get(key);
    }

    public synchronized String getPerf(long timeSpent) {
        StringBuilder result = new StringBuilder();
        Set<String> keys = storePerf.keySet();
        for(String key: keys){
            result.append("\n");
            result.append(key);
            result.append(" - ");
            result.append(((double)storePerf.get(key))/timeSpent);
            storePerf.put(key,0L);
        }
        result.append("\n");
        return result.toString();
    }
}
