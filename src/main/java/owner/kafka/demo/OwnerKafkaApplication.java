package owner.kafka.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OwnerKafkaApplication {
    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(OwnerKafkaApplication.class, args);
            System.out.println("启动成功");
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }
    }
}


