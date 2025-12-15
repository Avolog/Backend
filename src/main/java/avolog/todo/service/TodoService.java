package avolog.todo.service;

import avolog.todo.domain.PeriodType;
import avolog.todo.domain.Routine;
import avolog.todo.domain.Todo;
import avolog.todo.domain.TodoAssignee;
import avolog.todo.domain.TodoAssigneeId;
import avolog.todo.domain.TodoStatus;
import avolog.todo.dto.CompleteTodoRequest;
import avolog.todo.dto.CreateTodoRequest;
import avolog.todo.dto.TodoResponse;
import avolog.todo.dto.UpdateTodoRequest;
import avolog.todo.repository.CategoryRepository;
import avolog.todo.repository.RoutineRepository;
import avolog.todo.repository.TodoAssigneeRepository;
import avolog.todo.repository.TodoCompletionRepository;
import avolog.todo.repository.TodoRepository;
import avolog.todo.service.event.TodoEvent;
import avolog.todo.service.event.TodoEventPublisher;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final RoutineRepository routineRepository;
    private final CategoryRepository categoryRepository;
    private final TodoAssigneeRepository todoAssigneeRepository;
    private final TodoCompletionRepository todoCompletionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PeriodKeyCalculator periodKeyCalculator;
    private final TodoEventPublisher todoEventPublisher;

    public TodoService(
            TodoRepository todoRepository,
            RoutineRepository routineRepository,
            CategoryRepository categoryRepository,
            TodoAssigneeRepository todoAssigneeRepository,
            TodoCompletionRepository todoCompletionRepository,
            CurrentUserProvider currentUserProvider,
            PeriodKeyCalculator periodKeyCalculator,
            TodoEventPublisher todoEventPublisher
    ) {
        this.todoRepository = todoRepository;
        this.routineRepository = routineRepository;
        this.categoryRepository = categoryRepository;
        this.todoAssigneeRepository = todoAssigneeRepository;
        this.todoCompletionRepository = todoCompletionRepository;
        this.currentUserProvider = currentUserProvider;
        this.periodKeyCalculator = periodKeyCalculator;
        this.todoEventPublisher = todoEventPublisher;
    }

    public TodoResponse create(CreateTodoRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Routine routine = loadRoutineIfPresent(request.routineId());
        String periodKey = resolvePeriodKey(routine, request.todoDate());
        validateCategory(request.categoryId(), request.groupId(), userId);

        Todo todo = Todo.builder()
                .id(UUID.randomUUID())
                .creatorUserId(userId)
                .groupId(request.groupId())
                .categoryId(request.categoryId())
                .routineId(request.routineId())
                .title(request.title())
                .description(request.description())
                .status(TodoStatus.PENDING)
                .todoDate(request.todoDate())
                .periodKey(periodKey)
                .build();

        todoRepository.save(todo);
        replaceAssignees(todo.getId(), request.assigneeIds());
        publishCreatedEvent(todo);
        return TodoResponse.from(todo);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> list(UUID groupId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<Todo> todos = groupId != null
                ? todoRepository.findByGroupId(groupId)
                : todoRepository.findByCreatorUserId(userId);
        return todos.stream().map(TodoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse get(UUID id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        authorize(todo);
        return TodoResponse.from(todo);
    }

    public TodoResponse update(UUID id, UpdateTodoRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        authorize(todo);

        if (request.title() != null) {
            todo.setTitle(request.title());
        }
        if (request.description() != null) {
            todo.setDescription(request.description());
        }
        if (request.todoDate() != null) {
            todo.setTodoDate(request.todoDate());
            Routine routine = loadRoutineIfPresent(Optional.ofNullable(request.routineId()).orElse(todo.getRoutineId()));
            todo.setPeriodKey(resolvePeriodKey(routine, request.todoDate()));
        }
        if (request.categoryId() != null) {
            validateCategory(request.categoryId(), todo.getGroupId(), todo.getCreatorUserId());
            todo.setCategoryId(request.categoryId());
        }
        if (request.routineId() != null) {
            Routine routine = loadRoutineIfPresent(request.routineId());
            todo.setRoutineId(routine.getId());
            todo.setPeriodKey(resolvePeriodKey(routine, Optional.ofNullable(request.todoDate()).orElse(todo.getTodoDate())));
        }

        todoRepository.save(todo);
        if (request.assigneeIds() != null) {
            replaceAssignees(todo.getId(), request.assigneeIds());
        }
        publishUpdatedEvent(todo);
        return TodoResponse.from(todo);
    }

    public void delete(UUID id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        authorize(todo);
        todoAssigneeRepository.deleteByIdTodoId(todo.getId());
        todoRepository.delete(todo);
        publishDeletedEvent(todo);
    }

    public TodoResponse complete(UUID id, CompleteTodoRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
        authorize(todo);
        if (todo.getStatus() == TodoStatus.COMPLETED) {
            return TodoResponse.from(todo);
        }

        todo.setStatus(TodoStatus.COMPLETED);
        todoRepository.save(todo);

        OffsetDateTime completedAt = Optional.ofNullable(request.completedAt())
                .orElse(OffsetDateTime.now());

        todoCompletionRepository.save(avolog.todo.domain.TodoCompletion.builder()
                .id(UUID.randomUUID())
                .todoId(todo.getId())
                .routineId(todo.getRoutineId())
                .completedBy(currentUserProvider.getCurrentUserId())
                .periodKey(todo.getPeriodKey())
                .completedAt(completedAt)
                .build());

        publishCompletedEvent(todo);
        handleRoutineCompletion(todo);
        return TodoResponse.from(todo);
    }

    private void handleRoutineCompletion(Todo todo) {
        if (todo.getRoutineId() == null) {
            return;
        }

        Routine routine = routineRepository.findById(todo.getRoutineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Routine not found for todo"));

        long completed = todoCompletionRepository.countByRoutineIdAndPeriodKey(routine.getId(), todo.getPeriodKey());
        if (completed < routine.getTargetCount()) {
            return;
        }

        List<Todo> routineTodos = new ArrayList<>(todoRepository.findByRoutineIdAndPeriodKey(routine.getId(), todo.getPeriodKey()));
        boolean updated = false;
        for (Todo routineTodo : routineTodos) {
            if (routineTodo.getStatus() != TodoStatus.COMPLETED) {
                routineTodo.setStatus(TodoStatus.COMPLETED);
                updated = true;
            }
        }
        if (updated) {
            todoRepository.saveAll(routineTodos);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("routineId", routine.getId());
        data.put("period", routine.getPeriod().name());
        data.put("periodKey", todo.getPeriodKey());
        data.put("groupId", routine.getGroupId());
        data.put("categoryId", routine.getCategoryId());
        data.put("completedCount", completed);
        todoEventPublisher.publish(TodoEvent.of("RoutinePeriodCompleted", data));
    }

    private Routine loadRoutineIfPresent(UUID routineId) {
        if (routineId == null) {
            return null;
        }
        return routineRepository.findById(routineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Routine not found"));
    }

    private void validateCategory(UUID categoryId, UUID groupId, UUID ownerId) {
        if (categoryId == null) {
            return;
        }
        categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
        // TODO: enforce ownership/group scope when group service is available
    }

    private void authorize(Todo todo) {
        UUID currentUser = currentUserProvider.getCurrentUserId();
        if (todo.getGroupId() == null) {
            if (!todo.getCreatorUserId().equals(currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this todo");
            }
            return;
        }

        boolean isCreator = todo.getCreatorUserId().equals(currentUser);
        boolean isAssignee = todoAssigneeRepository.existsByIdTodoIdAndIdUserId(todo.getId(), currentUser);
        if (!isCreator && !isAssignee) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this todo");
        }
    }

    private void replaceAssignees(UUID todoId, List<UUID> assigneeIds) {
        todoAssigneeRepository.deleteByIdTodoId(todoId);
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return;
        }
        List<TodoAssignee> assignees = assigneeIds.stream()
                .distinct()
                .map(userId -> TodoAssignee.builder()
                        .id(new TodoAssigneeId(todoId, userId))
                        .todoId(todoId)
                        .userId(userId)
                        .build())
                .toList();
        todoAssigneeRepository.saveAll(assignees);
    }

    private String resolvePeriodKey(Routine routine, java.time.LocalDate todoDate) {
        if (routine == null) {
            return todoDate.toString();
        }
        PeriodType periodType = routine.getPeriod();
        return periodKeyCalculator.calculate(periodType, todoDate, routine.getWeekStartDay(), routine.getMonthBaseDay());
    }

    private void publishCreatedEvent(Todo todo) {
        Map<String, Object> data = baseTodoEventData(todo);
        todoEventPublisher.publish(TodoEvent.of("TodoCreated", data));
    }

    private void publishUpdatedEvent(Todo todo) {
        Map<String, Object> data = baseTodoEventData(todo);
        todoEventPublisher.publish(TodoEvent.of("TodoUpdated", data));
    }

    private void publishCompletedEvent(Todo todo) {
        Map<String, Object> data = baseTodoEventData(todo);
        data.put("status", TodoStatus.COMPLETED.name());
        todoEventPublisher.publish(TodoEvent.of("TodoCompleted", data));
    }

    private void publishDeletedEvent(Todo todo) {
        Map<String, Object> data = baseTodoEventData(todo);
        todoEventPublisher.publish(TodoEvent.of("TodoDeleted", data));
    }

    private Map<String, Object> baseTodoEventData(Todo todo) {
        Map<String, Object> data = new HashMap<>();
        data.put("todoId", todo.getId());
        data.put("routineId", todo.getRoutineId());
        data.put("groupId", todo.getGroupId());
        data.put("userId", todo.getCreatorUserId());
        data.put("status", todo.getStatus().name());
        data.put("todoDate", todo.getTodoDate().toString());
        data.put("periodKey", todo.getPeriodKey());
        return data;
    }
}
