package diploma.clustering.clusters;

import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;

import java.io.Serializable;

/**
 * @author Никита
 */
public class StatusesCluster extends Cluster<EnhancedStatus> implements Serializable {
    private TfIdf tfIdf;

    public StatusesCluster(int clusterId, double lambda) {
        super(clusterId, lambda);
        tfIdf = new TfIdf();
    }

    @Override
    public void assignPoint(EnhancedStatus point) {
//        super.assignPoint(point);
        size++;
        this.lastUpdateTime = System.currentTimeMillis();
        tfIdf.updateForNewDocument(Long.toString(point.getStatus().getId()), point.getNormalizedText());
    }

    public TfIdf getTfIdf() {
        return tfIdf;
    }
}
