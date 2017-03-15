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
public abstract class Clustering<C extends Cluster<T>, T> implements Serializable {
    protected List<C> clusters = new ArrayList<>();
    private long timestamp = 0;
    protected Double minSimilarity;

    public Clustering() {
    }

    public Clustering(Double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public abstract C findNearestCluster(T point);
    public abstract C createNewCluster();

    public List<C> getClusters() {
        return clusters;
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

    /**
     * Обработка следующей точки. Поиск ближайшего кластера и присоединение к нему точки.
     * Если такой кластер не найден, то создание нового
     * @param point - кластеризуемый элемент
     */
    public void processNext(T point) {
        timestamp++;
        C nearestCluster = findNearestCluster(point);
        if (nearestCluster == null) {
            C newCluster = createNewCluster();
            newCluster.assignPoint(point);
            newCluster.setLastUpdateTime(timestamp);
            addCluster(newCluster);
        } else {
            nearestCluster.assignPoint(point);
            nearestCluster.setLastUpdateTime(timestamp);
        }
    }

    /**
     * Добавление нового кластера
     * @param cluster - новый кластер
     */
    public void addCluster(C cluster) {
        clusters.add(cluster);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
