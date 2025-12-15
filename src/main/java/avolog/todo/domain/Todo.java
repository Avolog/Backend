package avolog.todo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "todos")
public class Todo extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "creator_user_id", nullable = false)
    private UUID creatorUserId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "routine_id")
    private UUID routineId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoStatus status;

    @Column(name = "todo_date", nullable = false)
    private LocalDate todoDate;

    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;
}
