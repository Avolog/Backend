package avolog.todo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TodoAssigneeId implements Serializable {

    @Column(name = "todo_id")
    private UUID todoId;

    @Column(name = "user_id")
    private UUID userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoAssigneeId that = (TodoAssigneeId) o;
        return Objects.equals(todoId, that.todoId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(todoId, userId);
    }
}
