package diploma.clustering.dbscan;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.dbscan.points.DbscanPoint;

import java.util.*;

/**
 * Алгоритм плотностной кластеризации DBSCAN
 * TODO: можно создать подкласс Dbscan, называющийся StatefulDbscan, который сохраняет состояние между вызовами метода run,
 * TODO: то есть запоминает все кластера и точки, пришедшие в этот объект Dbscan
 * @author Никита
 */
public class Dbscan {
    protected int minNeighboursCount;
    protected double eps;
//    protected K clustering;
    protected int lastClusterId = 1;
    protected SortedSet<Integer> usedIds = new TreeSet<>();

    public Dbscan() {}

    public Dbscan(int minNeighboursCount, double eps) {
        this.minNeighboursCount = minNeighboursCount;
        this.eps = eps;
    }

//    public abstract C addCluster(int clusterId);

    /**
     * Кластеризуемые элементы. В моем случае - это объекты класса
     * {@link diploma.clustering.dbscan.points.DbscanStatusesCluster}
     * @param points - кластеризуемые объекты
     */
    public void run(List<? extends DbscanPoint> points) {
        for (DbscanPoint point: points) {
            if (!point.isVisited()) {
//                point.setClusterId(lastClusterId);
                List<DbscanPoint> neighbours = (List<DbscanPoint>) point.getNeighbours(points, eps);
//                Cluster newCluster = clustering.createNewCluster();
                if (neighbours.size() >= minNeighboursCount) {
                    int clusterId = point.getLastAssignedClusterId() != 0 ? point.getLastAssignedClusterId() : getNextUnUsedId();
                    point.setClusterId(clusterId);
//                    newCluster.assignPoint(point);
                    while (!neighbours.isEmpty()) {
                        DbscanPoint lastNeighbour = neighbours.remove(neighbours.size() - 1);
                        if (!lastNeighbour.isAssigned()) {
//                            newCluster.assignPoint(lastNeighbour);
                            lastNeighbour.setClusterId(clusterId);
                            List<DbscanPoint> neighboursOfNeighbour = (List<DbscanPoint>) lastNeighbour.getNeighbours(points, eps);
                            if (neighboursOfNeighbour.size() >= minNeighboursCount) {
                                for (DbscanPoint neighbourOfNeighbour: neighboursOfNeighbour) {
                                    if (!neighbourOfNeighbour.isAssigned()) {
                                        neighbours.add(neighbourOfNeighbour);
                                    }
                                }
                            }
                        }
                    }
//                    clustering.addCluster(newCluster);
//                    lastClusterId++;
                } else point.setNoise();
            }
        }
    }

    private int getNextUnUsedId() {
        int newClusterId;
        try {
            newClusterId = usedIds.last() + 1;
        } catch (NoSuchElementException ex) {
            newClusterId = 1;
        }
        usedIds.add(newClusterId);
        return newClusterId;
    }

//    public K getClustering() {
//        return this.clustering;
//    }

}
