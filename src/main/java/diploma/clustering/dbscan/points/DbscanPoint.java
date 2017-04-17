package diploma.clustering.dbscan.points;

import java.util.List;

/**
 * Интерфейс элемента данных, кластеризуемых с помощью DBSCAN алгоритма.
 * В моем случае элемент - это {@link DbscanStatusesCluster},
 * в общем случае - это точка с координатами
 * @author Никита
 */
public interface DbscanPoint {
    public static final int NOISE = -1;
    public static final int UNVISITED = 0;
    /**
     * Поиск всех соседей точки
     * @param points - все точки, среди которых нужно искать соседей
     * @param eps - порог, по которому определяется является ли точка соседом или нет,
     *            может быть значением косинуса угла или евклидовым расстоянием
     * @return - список соседей
     */
    List<? extends DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps);
    void setNoise();

    default boolean isNoise() {
        return getClusterId() == NOISE;
    }

    default boolean isVisited() {
        return getClusterId() != UNVISITED;
    }

    /**
     * Проверяет была ли точка ассоциирована с каким-либо кластером
     * @return - true/false
     */
    default boolean isAssigned() {
        return getClusterId() > UNVISITED;
    }

    /**
     * Id кластера, к которому принадлежит элемент.
     * Может быть либо {@link diploma.clustering.dbscan.points.DbscanPoint#NOISE}, что означает,
     * что этот элемент является шумом, либо {@link diploma.clustering.dbscan.points.DbscanPoint#UNVISITED},
     * что означает, что этот элемент еще не был посещен и не входит ни в один кластер, либо числом больше 0,
     * что означает, что элемент принадлежит кластеру с этим Id.
     * Здесь под кластером понимается кластер, полученный с помощбю алгоритма DBSCAN
     * @return - id кластера
     */
    int getClusterId();
    void setClusterId(int clusterId);
    int getLastAssignedClusterId();
}
