package diploma.clustering.neuralgas.exceptions;

/**
 * @author Никита
 */
public class CannotCreateConnectionException extends RuntimeException {
    public CannotCreateConnectionException() {
        super("Невозможно создать связь между несуществующими нейронами");
    }
}
