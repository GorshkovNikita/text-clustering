package diploma.clustering.dbscan;

import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;

import java.util.List;

/**
 * Алгоритм плотностной кластеризации DBSCAN
 * @author Никита
 */
public abstract class Dbscan<K extends Clustering<C, T>, C extends Cluster<T>, T> {
    private int minNeighboursCount;
    private double eps;
    protected K clustering;

    // TODO: Нужно хранить все точки для поиска соседей новых поступивших точек
    private List<? extends DbscanPoint> allPoints;

    public Dbscan(int minNeighboursCount, double eps) {
        this.minNeighboursCount = minNeighboursCount;
        this.eps = eps;
    }

    // возможно вместо этого переделать под clazz.newInstance();, где класс
    // предается в конструктор
    public abstract C addCluster();

    /**
     * Кластеризуемые элементы. В моем случае - это объекты класса
     * {@link diploma.clustering.dbscan.points.DbscanStatusesCluster}
     * @param newPoints - кластеризуемые объекты
     */
    public void run(List<? extends DbscanPoint> newPoints) {
        for (DbscanPoint point: newPoints) {
            if (!point.isVisited()) {
                point.setVisited(true);
                List<DbscanPoint> neighbours = point.getNeighbours(newPoints, eps);
                // TODO: здесь нужно проверять id кластеров всех соседей.
                // TODO: если у ВСЕХ UNVISITED, то создать новый кластер
                // TODO: если у ВСЕХ одинаковый id > 0, то эта точка принадлежит кластеру с этим id
                // TODO: если у ВСЕХ NOISE, то проверить количество и
                // TODO: возможно создать новый кластер со всеми этими соседями
                // TODO: непонятно, что делать, если разные id соседей (??)

                // перенести внутрь if ?
                C newCluster = addCluster();
                if (neighbours.size() >= minNeighboursCount) {
                    newCluster.assignPoint((T) point);
                    while (!neighbours.isEmpty()) {
                        DbscanPoint lastNeighbour = neighbours.remove(neighbours.size() - 1);
                        if (!lastNeighbour.isVisited()) {
                            newCluster.assignPoint((T) lastNeighbour);
                            lastNeighbour.setVisited(true);
                            List<DbscanPoint> neighboursOfNeighbour = lastNeighbour.getNeighbours(newPoints, eps);
                            if (neighboursOfNeighbour.size() >= minNeighboursCount) {
                                // TODO: понять, отличаются или нет
//                                neighbours.addAll(neighboursOfNeighbour);
                                for (DbscanPoint neighbourOfNeighbour: neighboursOfNeighbour) {
                                    if (!neighbourOfNeighbour.isVisited()) {
                                        neighbours.add(neighbourOfNeighbour);
                                    }
                                }
                            }
                        }
                    }
                    clustering.addCluster(newCluster);
                }
                else point.setNoise(true);
            }
        }
    }
}
