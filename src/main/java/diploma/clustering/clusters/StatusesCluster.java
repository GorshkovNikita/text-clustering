package diploma.clustering.clusters;

import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;

import java.io.Serializable;

/**
 * @author Никита
 */
public class StatusesCluster extends Cluster<EnhancedStatus> implements Serializable {
    private TfIdf tfIdf;
    private StatusesClustering potentialSubClustering;
    private StatusesClustering outlierSubClustering;
    // Нужны только если есть саб-кластеризация
    private int mu;
    private double beta;

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

    /**
     * Присоединяем точку к ближайшему подкластеру или создаем новый
     * @param point - присоединямый твит
     */
    public void assignPointToSubCluster(EnhancedStatus point) {
        StatusesCluster nearestSubCluster = this.getPotentialSubClustering().findNearestCluster(point);
        if (nearestSubCluster == null) {
            nearestSubCluster = this.getOutlierSubClustering().findNearestCluster(point);
            if (nearestSubCluster == null) {
                StatusesCluster newSubCluster = new StatusesCluster(this.getOutlierSubClustering().getLastClusterId() + 1, this.lambda);
                newSubCluster.assignPoint(point);
                this.getOutlierSubClustering().addCluster(newSubCluster);
            }
            else {
                nearestSubCluster.assignPoint(point);
                if (nearestSubCluster.getWeight() > beta * mu) {
                    this.outlierSubClustering.getClusters().remove(nearestSubCluster);
                    nearestSubCluster.setId(this.potentialSubClustering.getLastClusterId() + 1);
                    this.potentialSubClustering.addCluster(nearestSubCluster);
                }
            }
        }
        else nearestSubCluster.assignPoint(point);
    }

    public TfIdf getTfIdf() {
        return tfIdf;
    }

    public void createSubClustering(double initSimilarity, int mu, double beta) {
        this.outlierSubClustering = new StatusesClustering(initSimilarity);
        this.potentialSubClustering = new StatusesClustering(initSimilarity);
        this.mu = mu;
        this.beta = beta;
    }

    public StatusesClustering getOutlierSubClustering() {
        return outlierSubClustering;
    }

    public StatusesClustering getPotentialSubClustering() {
        return potentialSubClustering;
    }
}
