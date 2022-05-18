import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Event {
    private Set<Consumer<EventArgs>> listeners = new HashSet();

    public void addListener(Consumer<EventArgs> listener) {
        listeners.add(listener);
    }

    public void broadcast(EventArgs args) {
        listeners.forEach(x -> x.accept(args));
    }

    public class EventArgs {
    }
}