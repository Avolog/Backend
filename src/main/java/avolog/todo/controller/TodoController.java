package avolog.todo.controller;

import avolog.todo.dto.CompleteTodoRequest;
import avolog.todo.dto.CreateTodoRequest;
import avolog.todo.dto.TodoResponse;
import avolog.todo.dto.UpdateTodoRequest;
import avolog.todo.service.TodoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/todos")
@Tag(name = "Todos", description = "Todo CRUD and completion")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    @Operation(summary = "Create todo", description = "Create a personal or group todo; routine-linked if routineId provided")
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List todos", description = "List personal todos or group todos when groupId is given")
    public List<TodoResponse> list(@Parameter(description = "Group id for group todos") @RequestParam(name = "groupId", required = false) UUID groupId) {
        return todoService.list(groupId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo", description = "Fetch a single todo by id")
    public TodoResponse get(@PathVariable UUID id) {
        return todoService.get(id);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete todo", description = "Mark todo as completed and trigger routine completion checks")
    public TodoResponse complete(@PathVariable UUID id, @RequestBody(required = false) CompleteTodoRequest request) {
        CompleteTodoRequest safeRequest = request == null ? new CompleteTodoRequest(null) : request;
        return todoService.complete(id, safeRequest);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update todo", description = "Patch title/description/date/category/routine/assignees")
    public TodoResponse update(@PathVariable UUID id, @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete todo", description = "Delete todo; routine-linked todos can be removed individually")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
