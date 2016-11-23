package diploma.clustering.dbscan.points;

import java.util.List;

/**
 * Интерфейс элемента данных, кластеризуемых с помощью DBSCAN алгоритма.
 * В моем случае элемент - это {@link DbscanStatusesCluster},
 * в общем случае - это точка с координатами
 * @author Никита
 */
public interface DbscanPoint {
    List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps);
    void setNoise(boolean isNoise);
    boolean isNoise();
    void setVisited(boolean isVisited);
    boolean isVisited();
}
