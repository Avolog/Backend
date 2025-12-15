package avolog.todo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo_completions")
public class TodoCompletion {

    @Id
    private UUID id;

    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Column(name = "routine_id")
    private UUID routineId;

    @Column(name = "completed_by", nullable = false)
    private UUID completedBy;

    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;
}
