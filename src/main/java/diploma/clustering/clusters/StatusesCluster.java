package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Никита
 */
public class StatusesCluster extends Cluster<EnhancedStatus> implements Serializable {
    private TfIdf tfIdf;
    private StatusesClustering potentialSubClustering;
    private StatusesClustering outlierSubClustering;
    private EnhancedStatus mostRelevantTweet;
    private int macroClusterId = 0;

    // Нужны только если есть саб-кластеризация
    private int mu;
    private double beta;

    public StatusesCluster(int clusterId, double lambda) {
        super(clusterId, lambda);
        this.tfIdf = new TfIdf();
    }

    @Override
    public void assignPoint(EnhancedStatus point) {
//        super.assignPoint(point);
        size++;
        processedPerTimeUnit++;
//        this.lastUpdateTime = System.currentTimeMillis();
        this.lastUpdateTime = point.getCreationTimestamp();
        this.actualUpdateTime = System.currentTimeMillis();
        tfIdf.updateForNewDocument(Long.toString(point.getStatus().getId()), point.getNormalizedText());
        if (this.mostRelevantTweet != null) {
            if (CosineSimilarity.cosineSimilarity(
                    tfIdf.getTfIdfForSpecificDocumentWithContent(point.getNormalizedText()),
                    tfIdf.getTfIdfForAllDocuments()) >
                CosineSimilarity.cosineSimilarity(
                    tfIdf.getTfIdfForSpecificDocumentWithContent(this.mostRelevantTweet.getNormalizedText()),
                    tfIdf.getTfIdfForAllDocuments()))
                this.mostRelevantTweet = point;
        }
        else this.mostRelevantTweet = point;
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
                if (nearestSubCluster.getWeight(point.getCreationTimestamp()) > beta * mu) {
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

    public int getMacroClusterId() {
        return macroClusterId;
    }

    public void setMacroClusterId(int macroClusterId) {
        this.macroClusterId = macroClusterId;
    }

    public EnhancedStatus getMostRelevantTweet() {
        return mostRelevantTweet;
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
