package owner.kafka.demo.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import owner.kafka.demo.exception.NoWarnException;
import owner.kafka.demo.service.KafkaConsumerService;

import java.util.Objects;

/**
 * <p>
 * 运单消费者
 * </p>
 *
 * @author dushuai
 * @version V1.0
 * @date 2022-07-20
 */

@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.kafka.instantiate", name = "enable", havingValue = "true")
public class WaybillConsumer {

    @Autowired
    private KafkaConsumerService consumerService;
    /**
     * 当一台服务器，concurrency=1时，验证一个消费组下的一个线程是否串行处理多个topic的任务
     * concurrency可以在配置文件中配置，也可以在注解中配置
     * 经本地测试：
     * 当topic只有一个分区时
     * 当使用方式一时，会为每个@KafkaListener创建一个消费者线程。不管concurrency是多少
     * 当使用方式二时，不管多少topic，只会创建一个消费者线程，串行处理，即便concurrency=3也不生效
     * 当topic有多个分区时
     * 当使用方式一时，concurrency=3时，@KafkaListener创建的线程数和topic对应的分区数有关
     * 当使用方式二时，concurrency=3会根据分区数生效
     * 总结：
     * 消费者组下的消费者线程，跟topic的分区数、@KafkaListener、concurrency有关
     * 1. 先看@KafkaListener，有几个@KafkaListener就至少会分配几个不同的消费线程
     * 2. 再看topic的分区数和concurrency，如果topic的分区数有多个，且concurrency>1，会分配多个，谁小取决于谁
     *  命名规范：org.springframework.kafka.KafkaListenerEndpointContainer#2-1-C-1
     *  0-0-C-1
     *  0-1-C-1
     *  1-0-C-1
     *  1-1-C-1
     *  2-0-C-1
     *  2-1-C-1
     *  第一个数字表示是哪个@KafkaListener，第二个1表示是哪个分区
     * @param record
     * @param ack
     */
    //方式一：
    @KafkaListener(topics = "${spring.kafka.orderTopicLocal}", groupId = "${spring.kafka.consumer.group-id}")
    @KafkaListener(topics = "${spring.kafka.topicOrderTest}", groupId = "${spring.kafka.consumer.group-id}")
    @KafkaListener(topics = "${spring.kafka.orderTopicTest}", groupId = "${spring.kafka.consumer.group-id}")
    //方式二：
    /*@KafkaListener(topics = {
            "${spring.kafka.orderTopicTest}",
            "${spring.kafka.orderTopicLocal}",
            "${spring.kafka.topicOrderTest}"},
            groupId = "${spring.kafka.consumer.group-id}", concurrency = "1")*/
    public void onMessage(ConsumerRecord record, Acknowledgment ack) {
        //log.info("kafka开始接受kafka消息");
        try {
            if (Objects.isNull(record) || Objects.isNull(record.value())) {
                log.error("fatalError,接受到的运单消息内容为空");
                return;
            }
            log.info("kafka消息,record:{}", record);
            /**
             * 思考：
             * 耗时的消息如何处理？单个耗时消息不要影响后续的正常消息消费
             * 场景：算法识别用旷世还是云丛，以来识别同步接口的返回结果
             * 先用云丛识别，云丛识别失败，再次用旷世识别（这个时间就会变大）
             * 1. 能否在处理消息前就能判断出来消息是否耗时
             * 2. 耗时的操作超过一定时间能否中断，将消息存储到耗时topic
             * 3. 能否使用多线程进行消费（多线程处理时要注意其他内存、cpu等问题）
             * 如果是调用三方接口，要设置好接口超时时间，不能一直消耗在接口调用上游方面
             */
            //consumerService.sing();
        } catch (Exception e) {
            /**
             * 如果没有throw，不影响后续消息的消费
             * 如果有throw，一条消息会重复消费10次，准确讲应该是1次正常调用+9次重试，后续重试是什么原理？
             * 所以尽量不要throw异常，即便有异常，不要进行重试，不要影响正常消息的消费,否则会造成消息的积压
             * 思考：异常的消息如何处理？
             * 1、不进行重试
             *
             */
            log.error("fatalError,WaybillConsumer处理消息异常e:", e);
            //throw new NoWarnException("消费kafka异常");
        } finally {
            ack.acknowledge();
        }
    }
}
