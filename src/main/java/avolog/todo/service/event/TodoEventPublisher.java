package avolog.todo.service.event;

public interface TodoEventPublisher {

    void publish(TodoEvent event);
}
