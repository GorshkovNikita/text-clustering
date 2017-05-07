package diploma.clustering.clusters;

import org.apache.commons.collections4.queue.CircularFifoQueue;

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
    protected long actualCreationTime;
    protected long actualUpdateTime;
    protected double lambda;
    protected int size;
    protected int processedPerTimeUnit = 0;
    private CircularFifoQueue<Integer> ratePerUnitQueue;

    /**
     * Идентификаторы кластеров, которые были слиты с данным кластером
     */
    private List<Integer> absorbedClusterIds = new ArrayList<>();

    public Cluster() {
//        this.assignedPoints = new ArrayList<>();
//        this.creationTime = System.currentTimeMillis();
        this.actualCreationTime = System.currentTimeMillis();
        this.ratePerUnitQueue = new CircularFifoQueue<>(10);
    }

    public Cluster(int id, double lambda) {
//        this.assignedPoints = new ArrayList<>();
        this.lambda = lambda;
        this.id = id;
        this.actualCreationTime = System.currentTimeMillis();
        this.ratePerUnitQueue = new CircularFifoQueue<>(10);
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
        this.actualUpdateTime = System.currentTimeMillis();
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
     * TODO: сделать как-то так, чтобы вес сохранялся после уменьшения, а потом проверить как это влияет
     */
    public double getWeight(long currentTimestamp) {
        long dt = currentTimestamp - lastUpdateTime;
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
        ratePerUnitQueue.add(this.processedPerTimeUnit);
        this.processedPerTimeUnit = 0;
    }

    public double getMeanRatePerUnit() {
        int sum = 0;
        for (int i = 0; i < ratePerUnitQueue.size(); i++)
            sum += ratePerUnitQueue.get(i);
        return sum / (double) ratePerUnitQueue.size();
    }

    public CircularFifoQueue<Integer> getRatePerUnitQueue() {
        return ratePerUnitQueue;
    }

    public int getProcessedPerTimeUnit() {
        return processedPerTimeUnit;
    }

    public long getActualCreationTime() {
        return actualCreationTime;
    }

    public long getActualUpdateTime() {
        return actualUpdateTime;
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
