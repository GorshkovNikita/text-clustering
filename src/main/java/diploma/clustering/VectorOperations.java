package diploma.clustering;

/**
 * @author Никита
 */
public class VectorOperations {
    public static Double[] addition(Double[] firstVector, Double[] secondVector) throws IllegalArgumentException {
        if (firstVector.length != secondVector.length)
            throw new IllegalArgumentException("Размерность векторов должна быть одинаковой");
        Double[] resultVector = new Double[firstVector.length];
        for (int i = 0; i < firstVector.length; ++i) {
            resultVector[i] = firstVector[i] + secondVector[i];
        }
        return resultVector;
    }

    public static Double[] scalarMultiplication(Double[] vector, Double scalar) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= scalar;
        }
        return vector;
    }

    public static Double euclideanDistance(Double[] firstVector, Double[] secondVector) throws IllegalArgumentException {
        if (firstVector.length != secondVector.length)
            throw new IllegalArgumentException("Размерность векторов должна быть одинаковой");
        Double result = 0.0;
        for (int i = 0; i < firstVector.length; i++) {
            result += Math.pow(secondVector[i] - firstVector[i], 2);
        }
        return Math.sqrt(result);
    }

    public static Double[] subtraction(Double[] firstVector, Double[] secondVector) {
        if (firstVector.length != secondVector.length)
            throw new IllegalArgumentException("Размерность векторов должна быть одинаковой");
        Double[] resultVector = new Double[firstVector.length];
        for (int i = 0; i < firstVector.length; ++i) {
            resultVector[i] = firstVector[i] - secondVector[i];
        }
        return resultVector;
    }

    public static Double cosineSimilarity(Double[] firstVector, Double[] secondVector) {
        if (firstVector.length != secondVector.length)
            throw new IllegalArgumentException("Размерность векторов должна быть одинаковой");
        Double dotProduct = 0.0, d1 = 0.0, d2 = 0.0;
        for (int i = 0; i < firstVector.length; i++) {
            dotProduct += firstVector[i] * secondVector[i];
            d1 += Math.pow(firstVector[i], 2);
            d2 += Math.pow(secondVector[i], 2);
        }
        return dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
    }
}
