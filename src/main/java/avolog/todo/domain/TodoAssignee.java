package avolog.todo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo_assignees")
public class TodoAssignee {

    @EmbeddedId
    private TodoAssigneeId id;

    @Column(name = "todo_id", insertable = false, updatable = false)
    private UUID todoId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;
}
