package diploma.clustering;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Никита
 */
public class CosineSimilarity {

    /**
     * Calculates the cosine similarity for two given vectors.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine similarity between the two vectors
     */
    public static Double cosineSimilarity(Map<String, Double> leftVector, Map<String, Double> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        Set<String> intersection = getIntersection(leftVector, rightVector);

        double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (Double value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        double d2 = 0.0d;
        for (Double value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;
        } else {
            cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
        }
        return cosineSimilarity;
    }

    /**
     * Returns a set with strings common to the two given maps.
     *
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(Map<String, Double> leftVector,
                                              Map<String, Double> rightVector) {
        Set<String> intersection = new HashSet<String>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Computes the dot product of two vectors. It ignores remaining elements. It means
     * that if a vector is longer than other, then a smaller part of it will be used to compute
     * the dot product.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dot(Map<String, Double> leftVector, Map<String, Double> rightVector,
                       Set<String> intersection) {
        double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

}
