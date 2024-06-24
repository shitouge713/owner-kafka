package owner.kafka.demo.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.*;

/**
 * <p>
 * 测试生产者
 * </p>
 *
 * @author dushuai
 * @version V1.0
 * @date 2022-07-20
 */
@Component
@Slf4j
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, String> template;

    @Value("${spring.kafka.orderTopicTest:order_topic_test}")
    private String orderTopicTest;

    @Value("${spring.kafka.topicOrderTest:topic_order_test}")
    private String topicOrderTest;

    @Value("${spring.kafka.orderTopicLocal:order_topic_local}")
    private String orderTopicLocal;

    private final static ThreadFactory NAMED_THREAD_FACTORY = r -> {
        String threadName = String.format("owner-pool-kafka-send-%d", Thread.currentThread().getId());
        Thread th = new Thread(r, threadName);
        return th;
    };
    private final static ExecutorService executorService = new ThreadPoolExecutor(20, 30, 500L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000), NAMED_THREAD_FACTORY);

    /**
     * 异步发送消息
     *
     * @return
     */
    public void sendMsg(String message) {
        //log.info(Thread.currentThread().getName() + ",开始发送消息,messageDTO:{}", message);
        try {
            ListenableFuture<SendResult<String, String>> future = template.send(orderTopicLocal, message);
            //方式一 java8 提供的异步回调方案
            /*CompletableFuture<SendResult<String, String>> completable = future.completable();
            completable.whenCompleteAsync((n, e) -> {
                if (null != e) {
                    log.error(Thread.currentThread().getName() + ",fatalError,发送报错了");
                } else {
                    log.info(Thread.currentThread().getName() + ",发送成功了！");
                }
            }, executorService);*/
            //方式二 guava提供的异步回调方案，推荐
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    //getRecordMetadata里面存在发送的topic和partition等信息
                    //log.info(Thread.currentThread().getName() + ",ProducerRecord:" + result.getProducerRecord());
                    //log.info(Thread.currentThread().getName() + ",RecordMetadata:" + result.getRecordMetadata());
                    log.info(Thread.currentThread().getName() + ",消息发送成功了！");
                }
                //发送消息失败回调
                @Override
                public void onFailure(Throwable ex) {
                    //ex.printStackTrace();
                    log.error("threadName:{},fatalError,消息发送报错了,messageDTO:{}", Thread.currentThread().getName(),message);
                }
            });
        } catch (Exception e) {
            log.error(Thread.currentThread().getName() + ":fatalError,订单消息异常,messageDTO:{},e:", message, e);
        }
        log.info(Thread.currentThread().getName() + ",发送消息完成");
    }

    public void sendMsg2(String message) {
        ListenableFuture<SendResult<String, String>> future = template.send(topicOrderTest, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("发送报错了");
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                System.out.println("发送成功了！");
            }
        });
    }

    public void sendMsg3(String message) {
        ListenableFuture<SendResult<String, String>> future = template.send(orderTopicTest, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("发送报错了");
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                System.out.println("发送成功了！");
            }
        });
    }
}
