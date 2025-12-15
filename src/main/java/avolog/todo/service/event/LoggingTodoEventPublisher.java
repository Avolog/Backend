package avolog.todo.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(TodoEventPublisher.class)
public class LoggingTodoEventPublisher implements TodoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingTodoEventPublisher.class);

    @Override
    public void publish(TodoEvent event) {
        // TODO: wire to SNS publisher once infrastructure is ready
        log.info("TodoEvent published: type={}, eventId={}, data={}", event.eventType(), event.eventId(), event.data());
    }
}
