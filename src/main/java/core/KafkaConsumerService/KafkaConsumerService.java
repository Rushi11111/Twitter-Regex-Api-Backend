package core.KafkaConsumerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Component
@EnableAsync
public class KafkaConsumerService {
    @Autowired
    ExecutorService executorService;

    @Autowired
    private ApplicationContext context;

    @Value("${kafkaconsumer.requestThresholdToStartInstantConsumer}")
    Integer requestThresholdToStartInstantConsumer;

    Map<String, Integer> instantConsumersRunning;
    Map<String, Integer> instantConsumerRequests;

    @Value("${kafkaconsumer.number}")
    Integer kafkaConsumerNumber;

    @Autowired
    public KafkaConsumerService() {
        instantConsumersRunning = new ConcurrentHashMap<>();
        instantConsumerRequests = new ConcurrentHashMap<>();
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void run() {
        int nThreads = kafkaConsumerNumber;

        for(int i = 0;i < nThreads;i++) {
            AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();

            KafkaTwitterConsumer kafkaConsume = ((DefaultListableBeanFactory) beanFactory).getBean(KafkaTwitterConsumer.class);
            kafkaConsume.setName("Group-1");

            executorService.submit(kafkaConsume);
        }
    }

    @Async
    public void instantRun(String regex, String groupId, long numRecords, long timeout) {

        if(instantConsumersRunning.containsKey(groupId)) {
            return;
        }
        Logger.getLogger("DEBUG").info(Integer.toString(requestThresholdToStartInstantConsumer));

        int currentRequest = instantConsumerRequests.getOrDefault(groupId, 0) + 1;
        instantConsumerRequests.put(groupId,currentRequest);

        if(currentRequest < requestThresholdToStartInstantConsumer)
            return;

        AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
        KafkaTwitterInstantConsumer kafkaInstantConsume = ((DefaultListableBeanFactory) beanFactory).getBean(KafkaTwitterInstantConsumer.class);
        kafkaInstantConsume.setRegex(regex);
        kafkaInstantConsume.setGroupId(groupId);
        kafkaInstantConsume.setTimeout(timeout);
        kafkaInstantConsume.setNumRecords(numRecords);

        instantConsumersRunning.put(groupId, 1);
        Future<?> future = executorService.submit(kafkaInstantConsume);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        instantConsumersRunning.remove(groupId);
    }
}

