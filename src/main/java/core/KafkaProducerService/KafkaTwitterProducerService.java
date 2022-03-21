package core.KafkaProducerService;

import core.PerfTracker.PerfStatsExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@EnableAsync
public class KafkaTwitterProducerService {

    Integer numProd = 2;
    List<KafkaTwitterProducer> twitterProducers = new ArrayList<>();

    @Autowired
    public KafkaTwitterProducerService(ApplicationContext context) {
        for(int i = 0;i < numProd;i++) {
            AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
            KafkaTwitterProducer twitterProducer = ((DefaultListableBeanFactory) beanFactory).getBean(KafkaTwitterProducer.class);
            twitterProducers.add(twitterProducer);
        }
    }

    @Autowired
    ExecutorService executorService;

    @Autowired
    PerfStatsExecutor perfStatsExecutor;

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void startThread() {
        executorService.submit(perfStatsExecutor);
        for(KafkaTwitterProducer twitterProducer : twitterProducers)
            executorService.submit(twitterProducer);
    }
}
