package avolog.groups.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class GroupEvent {
    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private String producer;
    private Object data;
}
