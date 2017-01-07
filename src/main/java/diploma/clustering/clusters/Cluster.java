package diploma.clustering.clusters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public abstract class Cluster<T> {
    private List<T> assignedPoints;
    /**
     * Идентификатор кластера
     */
    private int id;
    private long lastUpdateTime;

    public Cluster() {
        assignedPoints = new ArrayList<>();
    }

    public Cluster(int id) {
        assignedPoints = new ArrayList<>();
        this.id = id;
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

    public int getId() {
        return id;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
