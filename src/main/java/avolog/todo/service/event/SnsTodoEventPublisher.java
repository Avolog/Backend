package avolog.todo.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@ConditionalOnProperty(name = "todo.events.sns.enabled", havingValue = "true")
public class SnsTodoEventPublisher implements TodoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SnsTodoEventPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsTodoEventPublisher(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            @Value("${todo.events.sns.topic-arn}") String topicArn
    ) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    @Override
    public void publish(TodoEvent event) {
        String message = toJson(event);
        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageGroupId("todo-service")
                .build());
        log.info("TodoEvent published to SNS: type={}, eventId={}, topic={}", event.eventType(), event.eventId(), topicArn);
    }

    private String toJson(TodoEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
}
