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
     * @param points - кластеризуемые объекты
     */
    public void run(List<? extends DbscanPoint> points) {
        for (DbscanPoint point: points) {
            if (!point.isVisited()) {
                point.setVisited(true);
                List<DbscanPoint> neighbours = point.getNeighbours(points, eps);
                C newCluster = addCluster();
                if (neighbours.size() >= minNeighboursCount) {
                    newCluster.assignPoint((T) point);
                    while (!neighbours.isEmpty()) {
                        DbscanPoint lastNeighbour = neighbours.remove(neighbours.size() - 1);
                        if (!lastNeighbour.isVisited()) {
                            newCluster.assignPoint((T) lastNeighbour);
                            lastNeighbour.setVisited(true);
                            List<DbscanPoint> neighboursOfNeighbour = lastNeighbour.getNeighbours(points, eps);
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
                } else point.setNoise(true);
            }
        }
    }
}
