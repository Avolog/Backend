package avolog.todo.controller;

import avolog.todo.dto.CompleteTodoRequest;
import avolog.todo.dto.CreateTodoRequest;
import avolog.todo.dto.TodoResponse;
import avolog.todo.dto.UpdateTodoRequest;
import avolog.todo.service.TodoService;
import jakarta.validation.Valid;
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
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TodoResponse> list(@RequestParam(name = "groupId", required = false) UUID groupId) {
        return todoService.list(groupId);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable UUID id) {
        return todoService.get(id);
    }

    @PostMapping("/{id}/complete")
    public TodoResponse complete(@PathVariable UUID id, @RequestBody(required = false) CompleteTodoRequest request) {
        CompleteTodoRequest safeRequest = request == null ? new CompleteTodoRequest(null) : request;
        return todoService.complete(id, safeRequest);
    }

    @PatchMapping("/{id}")
    public TodoResponse update(@PathVariable UUID id, @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
