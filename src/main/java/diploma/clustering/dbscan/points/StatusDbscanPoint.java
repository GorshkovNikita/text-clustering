package diploma.clustering.dbscan.points;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.EnhancedStatus;
import diploma.clustering.tfidf.TfIdf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class StatusDbscanPoint implements DbscanPoint {
    private EnhancedStatus status;
    private int clusterId;
    private List<StatusDbscanPoint> neighbours = null;

    public StatusDbscanPoint(EnhancedStatus status) {
        this.status = status;
    }

    @Override
    public List<StatusDbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps) {
        if (this.neighbours == null) {
            List<StatusDbscanPoint> neighbours = new ArrayList<>();
            for (DbscanPoint point : points) {
                if (point != this) {
                    if (CosineSimilarity.cosineSimilarity(
                            TfIdf.getTfIdfForSpecificText(status.getNormalizedText()),
                            TfIdf.getTfIdfForSpecificText(((StatusDbscanPoint) point).getStatus().getNormalizedText())) >= eps)
                        neighbours.add((StatusDbscanPoint) point);
                }
            }
            this.neighbours = neighbours;
        }
        return neighbours;
    }

    @Override
    public void setNoise() {
        this.clusterId = NOISE;
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public int getLastAssignedClusterId() {
        return 0;
    }

    public EnhancedStatus getStatus() {
        return status;
    }
}
