package diploma.clustering.clusters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public abstract class Cluster<T> {
    private List<T> assignedPoints;
    private int clusterId;

    public Cluster() {

    }

    public Cluster(int clusterId) {
        assignedPoints = new ArrayList<>();
        this.clusterId = clusterId;
    }

    public Cluster(List<T> points) {
        assignedPoints = points;
    }

    public void assignPoint(T point) {
        assignedPoints.add(point);
    }

    public List<T> getAssignedPoints() {
        return assignedPoints;
    }

    public int getClusterId() {
        return clusterId;
    }
}
