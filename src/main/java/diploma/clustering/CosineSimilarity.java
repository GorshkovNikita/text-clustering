package diploma.clustering;

import diploma.clustering.tfidf.TfIdf;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Никита
 */
public class CosineSimilarity {

    /**
     * Calculates the cosine similarity for two given vectors.
     * d1 и d2 - это модули векторов (длина)
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

        double dotProduct = dotProduct(leftVector, rightVector, intersection);
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
            cosineSimilarity = dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
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
    private static Set<String> getIntersection(Map<String, Double> leftVector, Map<String, Double> rightVector) {
        Set<String> intersection = new HashSet<String>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Computes the dot product of two vectors. It ignores remaining elements. It means
     * that if a vector is longer than other, then a smaller part of it will be used to compute
     * the dot product.
     * Dot product = Скалярное произведение
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dotProduct(Map<String, Double> leftVector, Map<String, Double> rightVector, Set<String> intersection) {
        double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    public static void main(String[] args) {
        TfIdf tfIdf = new TfIdf();
        tfIdf.updateForNewDocument("doc1", "this is spartak moscow");
        tfIdf.updateForNewDocument("doc1", "that is spartak moscow");
        tfIdf.updateForNewDocument("doc2", "this is sparta");
        tfIdf.updateForNewDocument("doc2", "that was valencia");
        for (int i = 0; i < 100; i++) {
            String doc;
            if (i % 10 == 0)
                doc = "what's going on here?";
            else
                doc = "this is you and me";
            Map<String, Double> tfIdfVector1 = tfIdf.getTfIdfForSpecificDocumentWithContent(doc);
            Map<String, Double> tfIdfVector2 = tfIdf.getAugmentedTfIdfForAllDocuments(doc);
            double cos = CosineSimilarity.cosineSimilarity(tfIdfVector1, tfIdfVector2);
            tfIdf.updateForNewDocument("doc" + (i + 3), doc);
            System.out.println(cos);
        }
    }
}
