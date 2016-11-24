package diploma.clustering.dbscan.points;

import java.util.List;

/**
 * Интерфейс элемента данных, кластеризуемых с помощью DBSCAN алгоритма.
 * В моем случае элемент - это {@link DbscanStatusesCluster},
 * в общем случае - это точка с координатами
 * TODO: для точки {@link DbscanPoint} нужно указывать id кластера,
 * TODO: к которому она была присоединена
 * TODO: Возможно сделать также, как в online-denstream, где в id кластера
 * TODO: записывается либо NOISE (-1), UNVISITED (-2), либо id самого кластера (>0)
 * TODO: Это позволит удалить методы isNoise, isVisible
 * @author Никита
 */
public interface DbscanPoint {
    List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps);
    void setNoise(boolean isNoise);
    boolean isNoise();
    void setVisited(boolean isVisited);
    boolean isVisited();
}
