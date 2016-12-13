package diploma.clustering.clusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <C> - тип кластера
 * @param <T> - кластеризуемый тип данных
 * @author Никита
 */
public abstract class Clustering<C extends Cluster<T>, T> implements Serializable {
    protected List<C> clusters = new ArrayList<>();

    public abstract C findNearestCluster(T point);
    public abstract C createNewCluster();

    public List<C> getClusters() {
        return clusters;
    }

    public C findClusterById(int clusterId) {
        for(C cluster: clusters) {
            if (cluster.getClusterId() == clusterId)
                return cluster;
        }
        return null;
    }

    public void deleteClusterById(int clusterId) {
        for(C cluster: clusters) {
            if (cluster.getClusterId() == clusterId) {
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
            cluster.getAssignedPoints().forEach(firstCluster::assignPoint);
            clusters.remove(cluster);
        }
    }

    /**
     * Обработка следующей точки. Поиск ближайшего кластера и присоединение к нему точки.
     * Если такой кластер не найден, то создание нового
     * @param point - кластеризуемый элемент
     */
    public void processNext(T point) {
        C nearestCluster = findNearestCluster(point);
        if (nearestCluster == null) {
            C newCluster = createNewCluster();
            newCluster.assignPoint(point);
            addCluster(newCluster);
        } else nearestCluster.assignPoint(point);
    }

    /**
     * Добавление нового кластера
     * @param cluster - новый кластер
     */
    public void addCluster(C cluster) {
        clusters.add(cluster);
    }
}
