package diploma.clustering.neuralgas;

import diploma.clustering.neuralgas.exceptions.NeuronAlreadyExistsException;
import diploma.clustering.neuralgas.exceptions.NeuronNotExistsException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Никита
 */
public class NeuralGasTest {
    NeuralGas neuralGas;

    @Before
    public void setUp() {
        NeuralGas.clearStateForTesting();
        neuralGas = NeuralGas.getInstance();
        neuralGas.init(new Double[] {0.0, 0.0}, new Double[] {1.0, 1.0});
    }

    @Test
    public void testCreateConnectionBetweenNeurons() {
        Neuron firstNeuron = new Neuron(new Double[] {1.0, 0.0});
        Neuron secondNeuron = new Neuron(new Double[] {2.0, 4.0});
        neuralGas.createNewNeuron(firstNeuron);
        neuralGas.createNewNeuron(secondNeuron);
        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
        assertEquals(0, (int) neuralGas.getConnections().get(firstNeuron, secondNeuron));
        assertEquals(1, neuralGas.getNeurons().get(2).getNeighbours().size());
        assertEquals(1, neuralGas.getNeurons().get(3).getNeighbours().size());
        neuralGas.increaseAgeOfNeuronConnections(firstNeuron);
        assertEquals(1, (int) neuralGas.getConnections().get(firstNeuron, secondNeuron));
        // вставим еще одну связь, обновляя ее
        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
        assertEquals(1, neuralGas.getNeurons().get(2).getNeighbours().size());
        assertEquals(1, neuralGas.getNeurons().get(3).getNeighbours().size());
        assertEquals(0, (int) neuralGas.getConnections().get(firstNeuron, secondNeuron));
    }

    @Test(expected = NeuronNotExistsException.class)
    public void testCreateConnectionBetweenNotExistingNeurons() {
        Neuron firstNeuron = new Neuron(new Double[] {1.0, 0.0});
        Neuron secondNeuron = new Neuron(new Double[] {2.0, 4.0});
        firstNeuron.setId(2);
        secondNeuron.setId(3);
        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
        // TODO: перехватывается только первый эксепшн, разобраться
//        neuralGas.createNewNeuron(firstNeuron);
//        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
//        neuralGas.createNewNeuron(secondNeuron);
//        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
    }

    @Test
    public void testDeleteConnectionBetweenNeurons() {
        Neuron firstNeuron = new Neuron(new Double[] {1.0, 0.0});
        Neuron secondNeuron = new Neuron(new Double[] {2.0, 4.0});
        neuralGas.createNewNeuron(firstNeuron);
        neuralGas.createNewNeuron(secondNeuron);
        neuralGas.createConnectionBetweenNeurons(firstNeuron, secondNeuron);
        assertEquals(0, (int) neuralGas.getConnections().get(firstNeuron, secondNeuron));
        assertEquals(1, neuralGas.getNeurons().get(2).getNeighbours().size());
        assertEquals(1, neuralGas.getNeurons().get(3).getNeighbours().size());
        neuralGas.deleteConnectionBetweenNeurons(firstNeuron, secondNeuron);
        assertEquals(null, neuralGas.getConnections().get(firstNeuron, secondNeuron));
        assertEquals(null, neuralGas.getConnections().get(secondNeuron, firstNeuron));
        assertEquals(0, neuralGas.getNeurons().get(2).getNeighbours().size());
        assertEquals(0, neuralGas.getNeurons().get(3).getNeighbours().size());
    }

    @Test(expected = NeuronNotExistsException.class)
    public void testDeleteConnectionBetweenNotExistingNeurons() {
        Neuron firstNeuron = new Neuron(new Double[] {1.0, 0.0});
        Neuron secondNeuron = new Neuron(new Double[] {2.0, 4.0});
        firstNeuron.setId(2);
        secondNeuron.setId(3);
        neuralGas.deleteConnectionBetweenNeurons(firstNeuron, secondNeuron);
    }

    @Test
    public void testCreateNewNode() {
        Neuron newNeuron = new Neuron(new Double[] {11.0, 10.0});
        neuralGas.createNewNeuron(newNeuron);
        assertTrue(neuralGas.getNeurons().contains(newNeuron));
    }

    // TODO: в методе createNewNeuron перезаписывается id
//    @Test(expected = NeuronAlreadyExistsException.class)
//    public void testCreateExistingNewNode() {
//        Neuron newNeuron = new Neuron(new Double[] {10.0, 10.0});
//        neuralGas.createNewNeuron(newNeuron);
//    }

    @Test
    public void testGetMinimumDistanceBetweenGivenAndNearestNeurons() {
        Neuron neuron = new Neuron(new Double[] {15.0, 10.0});
        neuralGas.createNewNeuron(neuron);
        Double minimumDistance = neuralGas.getDistanceBetweenGivenAndNearestNeurons(neuron);
        assertEquals((Double) 16.643316977093238068928214785346, minimumDistance);
    }

    @Test(expected = NeuronNotExistsException.class)
    public void getDistanceBetweenWrongGivenAndNearestNeurons() {
        Neuron neuron = new Neuron(new Double[] {15.0, 10.0});
        neuron.setId(3);
        Double minimumDistance = neuralGas.getDistanceBetweenGivenAndNearestNeurons(neuron);
    }

    @Test
    public void testFindTwoNearestNeurons() {
        Neuron[] nearestNeurons = neuralGas.findTwoNearestNeurons(new Point(new Double[] {5.0, 6.0}));
        assertArrayEquals(new Neuron[] {neuralGas.getNeurons().get(1), neuralGas.getNeurons().get(0)}, nearestNeurons);
        neuralGas.createNewNeuron(new Neuron(new Double[]{6.0, 6.0}));
        neuralGas.createNewNeuron(new Neuron(new Double[]{5.0, 5.0}));
        nearestNeurons = neuralGas.findTwoNearestNeurons(new Point(new Double[] {5.5, 6.0}));
        assertArrayEquals(new Neuron[]{neuralGas.getNeurons().get(2), neuralGas.getNeurons().get(3)}, nearestNeurons);
    }

    @Test
    public void testIncreaseAgeOfNeuronConnections() {
        Neuron firstNeuron = neuralGas.getNeurons().get(0);
        Neuron secondNeuron = neuralGas.getNeurons().get(1);
        Neuron newNeuron = new Neuron(new Double[] {15.0, 9.0});
        neuralGas.createNewNeuron(newNeuron);
        neuralGas.createConnectionBetweenNeurons(newNeuron, secondNeuron);
        assertEquals((Integer) 0, neuralGas.getConnections().get(firstNeuron, secondNeuron));
        assertEquals((Integer) 0, neuralGas.getConnections().get(secondNeuron, newNeuron));
        // симметричные значения
        assertEquals((Integer) 0, neuralGas.getConnections().get(secondNeuron, firstNeuron));
        assertEquals((Integer) 0, neuralGas.getConnections().get(newNeuron, secondNeuron));

        neuralGas.increaseAgeOfNeuronConnections(secondNeuron);
        assertEquals((Integer) 1, neuralGas.getConnections().get(secondNeuron, firstNeuron));
        assertEquals((Integer) 1, neuralGas.getConnections().get(secondNeuron, newNeuron));
        // симметричные значения
        assertEquals((Integer) 1, neuralGas.getConnections().get(firstNeuron, secondNeuron));
        assertEquals((Integer) 1, neuralGas.getConnections().get(newNeuron, secondNeuron));
    }

    @Test
    public void testMoveNeuronAndItsNeighbours() {
        Neuron firstNeuron = neuralGas.getNeurons().get(0);
        Neuron secondNeuron = neuralGas.getNeurons().get(1);
        Neuron newNeuron = new Neuron(new Double[] {15.0, 9.0});
        neuralGas.createNewNeuron(newNeuron);
        neuralGas.createConnectionBetweenNeurons(newNeuron, secondNeuron);
        Point point = new Point(new Double[] {10.0, 9.0});
        neuralGas.getNeurons().get(1).attachPoint(point);
        neuralGas.moveNeuronAndItsNeighboursToPoint(secondNeuron, point);
        assertArrayEquals(firstNeuron.getCoordinatesVector(), new Double[]{0.1, 0.09});
        assertArrayEquals(secondNeuron.getCoordinatesVector(), new Double[]{10.0, 9.0});
        assertArrayEquals(newNeuron.getCoordinatesVector(), new Double[] {14.95, 9.0});

        point = new Point(new Double[] {4.0, 3.0});
        neuralGas.getNeurons().get(0).attachPoint(point);
        neuralGas.moveNeuronAndItsNeighboursToPoint(firstNeuron, point);
        assertArrayEquals(firstNeuron.getCoordinatesVector(), new Double[]{4.0, 3.0});
        assertArrayEquals(secondNeuron.getCoordinatesVector(), new Double[]{9.94, 8.94});
        assertArrayEquals(newNeuron.getCoordinatesVector(), new Double[] {14.95, 9.0});
    }

    @Test
    public void testFindNeuronWithMaximumError() {
        Neuron firstNeuron = neuralGas.getNeurons().get(0);
        firstNeuron.setError(2.0);
        Neuron secondNeuron = neuralGas.getNeurons().get(1);
        secondNeuron.setError(3.0);
        Neuron thirdNeuron = new Neuron(new Double[] {5.0, 5.0});
        neuralGas.createNewNeuron(thirdNeuron);
        thirdNeuron.setError(3.5);
        Neuron fourthNeuron = new Neuron(new Double[] {6.0, 6.0});
        neuralGas.createNewNeuron(fourthNeuron);
        fourthNeuron.setError(2.9);
        assertEquals(thirdNeuron, neuralGas.findNeuronWithMaximumError());
    }

    @Test
    public void testInsertNewNeuronBetween() {
        Neuron thirdNeuron = new Neuron(new Double[] {5.0, 5.0});
        neuralGas.createNewNeuron(thirdNeuron);
        thirdNeuron.setError(3.0);
        Neuron fourthNeuron = new Neuron(new Double[] {6.0, 6.0});
        neuralGas.createNewNeuron(fourthNeuron);
        fourthNeuron.setError(4.0);
        assertEquals(4, neuralGas.getNeurons().size());
        neuralGas.insertNewNeuronBetween(thirdNeuron, fourthNeuron);
        Neuron newNeuron = neuralGas.getNeurons().get(4);
        assertEquals(5, neuralGas.getNeurons().size());
        assertEquals((Double) 1.5, thirdNeuron.getError());
        assertEquals((Double) 2.0, fourthNeuron.getError());
        assertEquals(null, neuralGas.getConnections().get(thirdNeuron, fourthNeuron));
        assertEquals((Integer) 0, neuralGas.getConnections().get(thirdNeuron, newNeuron));
        assertEquals((Integer) 0, neuralGas.getConnections().get(newNeuron, fourthNeuron));
        assertEquals((Double) 1.5, newNeuron.getError());
        assertArrayEquals(new Double[]{5.5, 5.5}, newNeuron.getCoordinatesVector());
    }

    @Test(expected = NeuronNotExistsException.class)
    public void testInsertNewNeuronBetweenNotExistedNeurons() {
        Neuron thirdNeuron = new Neuron(new Double[] {5.0, 5.0});
        Neuron fourthNeuron = new Neuron(new Double[] {6.0, 6.0});
        neuralGas.insertNewNeuronBetween(thirdNeuron, fourthNeuron);
    }

    @Test
    public void testFindNearestNeuron() {
        Neuron thirdNeuron = new Neuron(new Double[] {5.0, 5.0});
        Neuron fourthNeuron = new Neuron(new Double[] {6.0, 6.0});
        neuralGas.createNewNeuron(thirdNeuron);
        neuralGas.createNewNeuron(fourthNeuron);
        Point point = new Point(new Double[] {5.3, 5.2});
        assertEquals(thirdNeuron, neuralGas.findNearestNeuron(point));
    }
}
