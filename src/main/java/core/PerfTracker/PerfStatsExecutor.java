package core.PerfTracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class PerfStatsExecutor implements Runnable{
    @Autowired
    PerfStatsTracker perfTracker;

    @Value("${perfTracker.enabled}")
    private boolean enabled;

    @Override
    public void run() {
        try {
            while(true && enabled) {
                long start = System.currentTimeMillis();
                Thread.sleep(20000);
                long end = System.currentTimeMillis();
                Logger.getLogger("PerfTracker").info(perfTracker.getPerf(end - start));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
