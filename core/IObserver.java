package core;
//SPRINT 3: NEW INTERFACE 
import java.util.Map;

public interface IObserver {
    public void updateObserver(String message);

    // event details for the web dashboard, does nothing unless an observer overrides it
    default void recordEvent(Map<String, Object> event) {}
}