package diploma.clustering.clusters;

import diploma.clustering.TextNormalizer;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public abstract class Cluster<T> {
    private List<T> assignedPoints;

    public Cluster() {
        assignedPoints = new ArrayList<>();
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
}
