package diploma.clustering.clusters;

import diploma.clustering.tfidf.TfIdf;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class Cluster<T> implements Serializable {
    private List<T> assignedPoints = new ArrayList<>();
    /**
     * Идентификатор кластера
     */
    protected int id;
    protected long creationTime;
    protected long lastUpdateTime;
    protected double lambda;
    protected int size;
    protected int processedPerTimeUnit = 0;

    /**
     * Идентификаторы кластеров, которые были слиты с данным кластером
     */
    private List<Integer> absorbedClusterIds = new ArrayList<>();

    public Cluster() {
//        this.assignedPoints = new ArrayList<>();
//        this.creationTime = System.currentTimeMillis();
    }

    public Cluster(int id, double lambda) {
//        this.assignedPoints = new ArrayList<>();
        this.lambda = lambda;
        this.id = id;
    }

    public Cluster(List<T> points) {
        assignedPoints = points;
    }

    public void assignPoint(T point) {
        // по идее это не нужно, только место занимает, но не всегда
        // можно переопределить в подклассе, если необходимо
        assignedPoints.add(point);
        size++;
        processedPerTimeUnit++;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public List<T> getAssignedPoints() {
        return assignedPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Вес кластера, уменьшающийся со временем, если он давно не обновлялся
     */
    public double getWeight() {
        long dt = System.currentTimeMillis() - lastUpdateTime;
        return (size * Math.pow(2, -lambda * dt));
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public List<Integer> getAbsorbedClusterIds() {
        return absorbedClusterIds;
    }

    public int getSize() {
        return size;
    }

    public void resetProcessedPerTimeUnit() {
        this.processedPerTimeUnit = 0;
    }

    public int getProcessedPerTimeUnit() {
        return processedPerTimeUnit;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Cluster)) return false;

        Cluster cluster = (Cluster) other;
        if (this.id != cluster.id) return false;
        if (this.size != cluster.size) return false;
        if (this.creationTime != cluster.creationTime) return false;
        if (this.lastUpdateTime != cluster.lastUpdateTime) return false;
//        if (!this.absorbedClusterIds.equals(cluster.absorbedClusterIds)) return false;
//        if (!this.assignedPoints.equals(cluster.assignedPoints)) return false;
        return true;
    }
}
