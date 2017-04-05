package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.EnhancedStatus;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Никита
 */
public class StatusesClustering extends Clustering<StatusesCluster, EnhancedStatus> {

    public StatusesClustering() {
        super();
    }

    public StatusesClustering(Double minSimilarity) {
        super(minSimilarity);
    }

    @Override
    public StatusesCluster findNearestCluster(EnhancedStatus point) {
        StatusesCluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (StatusesCluster cluster: getClusters()) {
            Map<String, Double> tfIdfForAllDocuments = cluster.getTfIdf().getTfIdfForAllDocuments();
            Map<String, Double> tfIdfOfDocumentIntersection = cluster.getTfIdf().getTfIdfOfDocumentIntersection(point.getNormalizedText());
            Double similarity = CosineSimilarity.cosineSimilarity(tfIdfForAllDocuments, tfIdfOfDocumentIntersection);
            if (similarity > minSimilarity && similarity > maxSimilarity) {
                nearestCluster = cluster;
                maxSimilarity = similarity;
            }
        }
        return nearestCluster;
    }
}
