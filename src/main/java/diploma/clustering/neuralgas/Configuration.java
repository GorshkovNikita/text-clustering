package diploma.clustering.neuralgas;

/**
 * @author Никита
 */
public class Configuration {
    public static final int MAX_NUMBER_OF_NEURONS = Integer.MAX_VALUE;
    /**
     * Максимальное время жизни связи.
     * По идее при уменьшени этого числа алгоритм должен быстрее обучаться
     */
    public static final int MAX_AGE_OF_CONNECTION = 6;
    public static final int STEP_FOR_NEW_NEURON = 10000;
    public static final double ERROR_REDUCTION_FACTOR_AFTER_INSERTION = 0.5;
    public static final double ERROR_REDUCTION_FACTOR = 0.95;
}
