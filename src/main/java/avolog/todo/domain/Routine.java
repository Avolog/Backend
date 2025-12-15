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
@Table(name = "routines")
public class Routine extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "routine_type", nullable = false, length = 10)
    private RoutineType routineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PeriodType period;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "week_start_day")
    private Integer weekStartDay;

    @Column(name = "month_base_day")
    private Integer monthBaseDay;

    @Column(name = "creator_user_id", nullable = false)
    private UUID creatorUserId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "category_id")
    private UUID categoryId;
}
