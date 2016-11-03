package diploma.clustering;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Никита
 */
public class VectorOperationsTest {
    @Before
    public void setUp() {
    }

    @Test
    public void testEuclideanTwoDimensionalDistance() {
        Double[] firstVector = new Double[] {0.0, 0.0};
        Double[] secondVector = new Double[] {3.0, 4.0};
        assertEquals((Double) 5.0, VectorOperations.euclideanDistance(firstVector, secondVector));
    }

    @Test
    public void testEuclideanThreeDimensionalDistance() {
        Double[] firstVector = new Double[] {0.0, 0.0, 0.0};
        Double[] secondVector = new Double[] {3.0, 4.0, 4.0};
        assertEquals((Double) 6.4031242374328486864882176746218, VectorOperations.euclideanDistance(firstVector, secondVector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionIsThrown() {
        Double[] firstWrongVector = new Double[] {0.0, 0.0};
        Double[] secondWrongVector = new Double[] {3.0, 4.0, 5.0};
        VectorOperations.euclideanDistance(firstWrongVector, secondWrongVector);
    }

    @Test
      public void testAddition() {
        Double[] firstVector = new Double[] {2.0, 10.0, 4.0};
        Double[] secondVector = new Double[] {3.0, 4.0, 4.0};
        assertArrayEquals(new Double[] {5.0, 14.0, 8.0}, VectorOperations.addition(firstVector, secondVector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdditionWithDifferentDimensionalVectors() {
        Double[] firstVector = new Double[] {2.0, 10.0, 4.0};
        Double[] secondVector = new Double[] {3.0, 4.0, 4.0, 6.0};
        VectorOperations.addition(firstVector, secondVector);
    }

    @Test
    public void testSubtraction() {
        Double[] firstVector = new Double[] {2.0, 10.0, 4.0};
        Double[] secondVector = new Double[] {3.0, 4.0, 4.0};
        assertArrayEquals(new Double[] {-1.0, 6.0, 0.0}, VectorOperations.subtraction(firstVector, secondVector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractionWithDifferentDimensionalVectors() {
        Double[] firstVector = new Double[] {2.0, 10.0, 4.0};
        Double[] secondVector = new Double[] {3.0, 4.0, 4.0, 6.0};
        VectorOperations.subtraction(firstVector, secondVector);
    }

    @Test
    public void testScalarMultiplication() {
        Double[] vector = new Double[] {2.0, 10.0, 4.0};
        assertArrayEquals(new Double[] {6.0, 30.0, 12.0}, VectorOperations.scalarMultiplication(vector, 3.0));
        assertArrayEquals(new Double[] {-6.0, -30.0, -12.0}, VectorOperations.scalarMultiplication(vector, -1.0));
    }
}
