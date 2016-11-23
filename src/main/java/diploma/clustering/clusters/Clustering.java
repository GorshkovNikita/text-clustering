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