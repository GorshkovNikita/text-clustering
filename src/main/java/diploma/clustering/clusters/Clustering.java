package diploma.clustering.clusters;

import diploma.clustering.Point;
import diploma.clustering.dbscan.points.DbscanPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <C> - тип кластера
 * @param <T> - кластеризуемый тип данных
 * @author Никита
 */
public class Clustering<C extends Cluster<T>, T> implements Serializable {
    protected List<C> clusters = new ArrayList<>();
//    protected long timestamp = 0;
    protected Double minSimilarity;
    // id последнего добавленного кластера, то есть при добавлении нового нужно увеличить это число на 1
    protected int lastClusterId;

    public Clustering() {}

    public Clustering(Double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public C findNearestCluster(T point) {
        return null;
    }
//    public abstract C createNewCluster();

    public List<C> getClusters() {
        return clusters;
    }

    /**
     * Добавление нового кластера
     * @param cluster - новый кластер
     */
    public void addCluster(C cluster) {
        clusters.add(cluster);
        cluster.setCreationTime(System.currentTimeMillis());
        lastClusterId = cluster.getId();
    }

    public C findClusterById(int clusterId) {
        for(C cluster: clusters) {
            if (cluster.getId() == clusterId)
                return cluster;
        }
        return null;
    }

    public void deleteClusterById(int clusterId) {
        for(C cluster: clusters) {
            if (cluster.getId() == clusterId) {
                clusters.remove(cluster);
                return;
            }
        }
    }

    /**
     * Слияние двух или более кластеров в один. При этом все элементы
     * ассоциируются с первым кластером. После чего остальные кластера удаляются
     * @param firstCluster - первый кластер
     * @param otherClusters - остальные кластера
     */
    public final void mergeClusters(C firstCluster, List<C> otherClusters) {
        for (C cluster: otherClusters) {
            cluster.getAssignedPoints().forEach((point) -> {
                firstCluster.assignPoint(point);
                if (point instanceof DbscanPoint)
                    ((DbscanPoint) point).setClusterId(firstCluster.getId());
            });
            firstCluster.getAbsorbedClusterIds().add(cluster.getId());
            clusters.remove(cluster);
        }
    }

    public int getLastClusterId() {
        return lastClusterId;
    }

    /**
     * Обработка следующей точки. Поиск ближайшего кластера и присоединение к нему точки.
     * Если такой кластер не найден, то создание нового
     * @param point - кластеризуемый элемент
     */
//    public void processNext(T point) {
//        timestamp++;
//        C nearestCluster = findNearestCluster(point);
//        if (nearestCluster == null) {
//            C newCluster = createNewCluster();
//            newCluster.assignPoint(point);
//            newCluster.setLastUpdateTime(timestamp);
//            addCluster(newCluster);
//        } else {
//            nearestCluster.assignPoint(point);
//            nearestCluster.setLastUpdateTime(timestamp);
//        }
//    }

//    public long getTimestamp() {
//        return timestamp;
//    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Clustering)) return false;

        Clustering clustering = (Clustering) other;

        if (this.lastClusterId != clustering.lastClusterId) return false;
        if (!this.minSimilarity.equals(clustering.minSimilarity)) return false;
        if (!this.clusters.equals(clustering.clusters)) return false;

        return true;
    }
}
