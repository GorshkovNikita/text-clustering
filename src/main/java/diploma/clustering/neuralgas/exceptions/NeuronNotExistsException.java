package diploma.clustering.neuralgas.exceptions;

/**
 * Exception, который выкидывается при попытке использовать нейрон,
 * который не существует в сети. Например, при создании связи или поиске
 * расстояния между нейронами
 * @author Никита
 */
public class NeuronNotExistsException extends RuntimeException {
    public NeuronNotExistsException() {
        super("Заданного нейрона не существует в сети");
    }
}
