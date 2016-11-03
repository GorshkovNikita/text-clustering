package diploma.clustering.neuralgas.exceptions;

/**
 * Exception, который выкидывается при добавлении в сеть нейрона,
 * с координатами уже находящегося в сети нейрона
 * @author Никита
 */
public class NeuronAlreadyExistsException extends RuntimeException {
    public NeuronAlreadyExistsException() {
        super("Невозможно создать нейроны с одинаковыми координатами");
    }
}
