package diploma.clustering.neuralgas.exceptions;

/**
 * @author Никита
 */
public class NeuralGasIsNotInitialized extends RuntimeException {
    public NeuralGasIsNotInitialized() {
        super("Нейроннный газ должен быть инициализирован с помощью метода init()");
    }
}
