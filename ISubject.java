//SPRINT 3: NEW INTERFACE

public interface ISubject {
    public void addObserver(IObserver observer);
    public void notifyObserver(String message);
}
