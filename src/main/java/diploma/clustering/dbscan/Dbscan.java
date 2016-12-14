package diploma.clustering.dbscan;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.dbscan.points.DbscanPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Алгоритм плотностной кластеризации DBSCAN
 * TODO: можно создать подкласс Dbscan, называющийся StatefulDbscan, который сохраняет состояние между вызовами метода run,
 * TODO: то есть запоминает все кластера и точки, пришедшие в этот объект Dbscan
 * @author Никита
 */
public abstract class Dbscan<K extends Clustering<C, T>, C extends Cluster<T>, T> {
    private int minNeighboursCount;
    private double eps;
    protected K clustering;
    private int lastClusterId = 1;

    private List<DbscanPoint> allPoints = new ArrayList<>();

    public Dbscan(int minNeighboursCount, double eps) {
        this.minNeighboursCount = minNeighboursCount;
        this.eps = eps;
    }

    public abstract C addCluster(int clusterId);

    /**
     * Кластеризуемые элементы. В моем случае - это объекты класса
     * {@link diploma.clustering.dbscan.points.DbscanStatusesCluster}
     * @param newPoints - кластеризуемые объекты
     */
    public void run(List<? extends DbscanPoint> newPoints) {
        allPoints.addAll(newPoints);
        for (DbscanPoint point: newPoints) {
            if (!point.isVisited()) {
                List<DbscanPoint> neighbours = point.getNeighbours(allPoints, eps);
                if (neighbours.size() >= minNeighboursCount) {
                    HashSet<Integer> neighbourClusterIds = getSetOfNeighbourClusterIds(neighbours);
                    // все соседи относятся к одному и тому же кластеру
                    if (neighbourClusterIds.size() == 1 && neighbourClusterIds.iterator().next() > DbscanPoint.UNVISITED) {
                        int clusterId = neighbours.get(0).getClusterId();
                        point.setClusterId(clusterId);
                        clustering.findClusterById(clusterId).assignPoint((T) point);
                    }
                    // все соседи относятся к различным кластерам (шума и непройденных вершин нет) => просто объединяем кластера в один
                    else if (neighbourClusterIds.size() > 1 &&
                            neighbourClusterIds.stream().filter((id) -> id > DbscanPoint.UNVISITED).count() == neighbourClusterIds.size()) {
                        List<C> otherClusters = new ArrayList<>();
                        Iterator<Integer> iterator = neighbourClusterIds.iterator();
                        C resultCluster = null;
                        while (iterator.hasNext()) {
                            Integer nextId = iterator.next();
                            if (nextId > DbscanPoint.UNVISITED && resultCluster == null)
                                resultCluster = clustering.findClusterById(nextId);
                            else otherClusters.add(clustering.findClusterById(nextId));
                        }
                        clustering.mergeClusters(resultCluster, otherClusters);
                        resultCluster.assignPoint((T) point);
                    }
                    // все соседи в одном кластере + NOISE + UNVISITED
                    else if (neighbourClusterIds.size() > 1 &&
                            neighbourClusterIds.stream().
                                    filter((id) -> id != DbscanPoint.UNVISITED && id != DbscanPoint.NOISE).count() == 1) {
                        Iterator<Integer> iterator = neighbourClusterIds.iterator();
                        int clusterId;
                        do {
                            clusterId = iterator.next();
                        } while (clusterId <= DbscanPoint.UNVISITED);
                        C cluster = clustering.findClusterById(clusterId);
                        point.setClusterId(clusterId);
                        cluster.assignPoint((T) point);
                        expandCluster(cluster, neighbours);
                    }
                    // TODO: нет проверки на то, если соседи относятся к разным кластерам + есть NOISE и UNVISITED
                    else {
                        C newCluster = addCluster(lastClusterId);
                        newCluster.assignPoint((T) point);
                        point.setClusterId(newCluster.getClusterId());
                        expandCluster(newCluster, neighbours);
                        clustering.addCluster(newCluster);
                        lastClusterId++;
                    }
                }
                else point.setNoise();
            }
        }
    }

    /**
     * Метод для проверки id найденных соседей
     * В данный момент вовзвращает true тогда и только тогда, когда
     * id кластера всех соседей одинаков и больше 0 {@link diploma.clustering.dbscan.points.DbscanPoint#UNVISITED},
     * то есть точку, у которой этих соседей нашли нужно прицепить к этому кластеру ИЛИ
     * если все соседи еще не были посещены или являются шумом
     * @param neighbours - соседи точки
     * @return - множество
     */
    private HashSet<Integer> getSetOfNeighbourClusterIds(List<? extends DbscanPoint> neighbours) {
        HashSet<Integer> neighbourClusterIds = new HashSet<>();
        for (DbscanPoint neighbour : neighbours) {
            neighbourClusterIds.add(neighbour.getClusterId());
        }
        return neighbourClusterIds;
    }

    /**
     * Добавление в кластер новых элементов, основываясь на соседях точки
     * @param cluster - расширяемый кластер
     * @param neighbours - соседи точки
     */
    private void expandCluster(C cluster, List<DbscanPoint> neighbours) {
        while (!neighbours.isEmpty()) {
            DbscanPoint lastNeighbour = neighbours.remove(neighbours.size() - 1);
            if (lastNeighbour.getClusterId() != cluster.getClusterId()) {
                if (lastNeighbour.isAssigned()) {
                    C assignedCluster = clustering.findClusterById(lastNeighbour.getClusterId());
                    assignedCluster.getAssignedPoints().remove(lastNeighbour);
                }
                lastNeighbour.setClusterId(cluster.getClusterId());
                cluster.assignPoint((T) lastNeighbour);
                List<DbscanPoint> neighboursOfNeighbour = lastNeighbour.getNeighbours(allPoints, eps);
                if (neighboursOfNeighbour.size() >= minNeighboursCount)
                    // TODO: здесь нужно проверять также, к кому относятся эти соседи (?)
                    for (DbscanPoint neighbourOfNeighbour: neighboursOfNeighbour)
                        if (neighbourOfNeighbour.getClusterId() != cluster.getClusterId())
                            neighbours.add(neighbourOfNeighbour);
            }
        }
    }

}
