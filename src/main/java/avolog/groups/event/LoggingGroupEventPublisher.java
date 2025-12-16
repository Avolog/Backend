package avolog.groups.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingGroupEventPublisher implements GroupEventPublisher {
    @Override
    public void publish(GroupEvent event) {
        // Stub publisher for now; replace with SNS/SQS integration later.
        log.info("Publishing group event: type={} id={} data={}", event.getEventType(), event.getEventId(), event.getData());
    }
}
