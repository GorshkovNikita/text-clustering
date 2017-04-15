package diploma.clustering.clusters;

import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

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

    public static class MapDbSerializer implements Serializer<StatusesCluster>, Serializable {
        @Override
        public void serialize(DataOutput2 out, StatusesCluster value) throws IOException {
            out.writeInt(value.id);
            out.writeInt(value.size);
            out.writeLong(value.creationTime);
            out.writeLong(value.lastUpdateTime);
            new TfIdf.MapDbSerializer().serialize(out, value.tfIdf);
        }

        @Override
        public StatusesCluster deserialize(DataInput2 input, int available) throws IOException {
            StatusesCluster statusesCluster = new StatusesCluster(input.readInt(), 0.0002);
            statusesCluster.size = input.readInt();
            statusesCluster.creationTime = input.readLong();
            statusesCluster.lastUpdateTime = input.readLong();
            statusesCluster.tfIdf = new TfIdf.MapDbSerializer().deserialize(input, -1);
            return statusesCluster;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusesCluster)) return false;
        if (!super.equals(o)) return false;

        StatusesCluster cluster = (StatusesCluster) o;

        if (!tfIdf.equals(cluster.tfIdf)) return false;

        return true;
    }
}
