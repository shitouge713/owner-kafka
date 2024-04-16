package owner.kafka.demo.service;

import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {
    @Override
    public void sing() {
        //模拟发生异常时，消费者是否还能正常消费
        int num1 = 10;
        int num2 = 0;
        int i = num1 / num2;
    }
}
