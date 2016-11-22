package diploma.clustering.neuralgas;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Никита
 */
public class NeuronTest {
    NeuralGas neuralGas;

    @Before
    public void setUp() {
        NeuralGas.clearStateForTesting();
        neuralGas = NeuralGas.getInstance();
        neuralGas.init(new Double[] {0.0, 0.0}, new Double[] {10.0, 10.0});
    }

    // TODO: пересчитать новый Threshold
//    @Test
//    public void testGetInitialThreshold() {
//        assertEquals((Double) 7.0710678118654752440084436210485, neuralGas.getNeurons().get(0).getThreshold());
//        assertEquals((Double) 7.0710678118654752440084436210485, neuralGas.getNeurons().get(1).getThreshold());
//    }
//
//    @Test
//    public void testGetThreshold() {
//        Neuron neuron = neuralGas.getNeurons().get(0);
//        neuron.attachPoint(new Point(new Double[]{7.0, 9.0}));
//        neuron.attachPoint(new Point(new Double[]{5.0, 5.0}));
//        assertEquals((Double) 9.236411031428427517684466938358, neuron.getThreshold());
//        Neuron neighbourNeuron = neuralGas.getNeurons().get(1);
//        neighbourNeuron.attachPoint(new Point(new Double[]{1.0, 2.0}));
//        neighbourNeuron.attachPoint(new Point(new Double[]{6.0, 0.0}));
//        assertEquals((Double) 11.689273327579689002850677090228, neuron.getThreshold());
//        neuron.attachPoint(new Point(new Double[]{3.0, 4.0}));
//        assertEquals((Double) 10.351418662063751202280541672182, neuron.getThreshold());
//    }

    @Test
    public void testAssignPoint() {
        Neuron neuron = neuralGas.getNeurons().get(0);
        assertEquals((Double) 0.0, neuron.getTotalDistanceBetweenNeuronAndItsPoints());
        neuron.attachPoint(new Point(new Double[]{5.0, 5.0}));
        assertEquals((Double) 7.0710678118654752440084436210485, neuron.getTotalDistanceBetweenNeuronAndItsPoints());
        neuron.attachPoint(new Point(new Double[]{7.0, 9.0}));
        assertEquals((Double) 18.472822062856855035368933876716, neuron.getTotalDistanceBetweenNeuronAndItsPoints());
    }

    @Test
    public void testFindNeighbourWithMaximumError() {
        Neuron firstNeuron = neuralGas.getNeurons().get(0);
        firstNeuron.setError(2.0);
        Neuron secondNeuron = neuralGas.getNeurons().get(1);
        secondNeuron.setError(3.0);
        Neuron thirdNeuron = new Neuron(new Double[] {5.0, 5.0});
        neuralGas.createNewNeuron(thirdNeuron);
        thirdNeuron.setError(3.5);
        Neuron fourthNeuron = new Neuron(new Double[] {6.0, 6.0});
        neuralGas.createNewNeuron(fourthNeuron);
        fourthNeuron.setError(3.1);
        neuralGas.createConnectionBetweenNeurons(firstNeuron, fourthNeuron);
        assertEquals(null, thirdNeuron.findNeighbourWithMaximumError());
        assertEquals(fourthNeuron, firstNeuron.findNeighbourWithMaximumError());
    }

    @Test
    public void testDetachPoint() {
        Neuron firstNeuron = neuralGas.getNeurons().get(0);
        assertEquals((Double) 0.0, firstNeuron.getTotalDistanceBetweenNeuronAndItsPoints());
        Point point = new Point(new Double[] {1.0, 1.0});
        Point secondPoint = new Point(new Double[] {4.0, 4.0});
        firstNeuron.attachPoint(point);
        firstNeuron.attachPoint(secondPoint);
        assertEquals(2, firstNeuron.getPoints().size());
        assertEquals((Double) 7.0710678118654752440084436210485, firstNeuron.getTotalDistanceBetweenNeuronAndItsPoints());
        firstNeuron.detachPoint(point);
        assertEquals((Double) 5.6568542494923801952067548968388, firstNeuron.getTotalDistanceBetweenNeuronAndItsPoints());
        assertEquals(1, firstNeuron.getPoints().size());
        firstNeuron.detachPoint(secondPoint);
        assertEquals((Double) 0.0, firstNeuron.getTotalDistanceBetweenNeuronAndItsPoints());
        assertEquals(0, firstNeuron.getPoints().size());
    }
}
