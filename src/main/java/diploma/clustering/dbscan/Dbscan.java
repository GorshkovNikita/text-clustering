package diploma.clustering.dbscan;

import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * Алгоритм плотностной кластеризации DBSCAN
 * @author Никита
 */
public abstract class Dbscan<K extends Clustering<C, T>, C extends Cluster<T>, T> {
    private int minNeighboursCount;
    private double eps;
    protected K clustering;
    private int lastClusterId = 1;

    // TODO: Нужно хранить все точки для поиска соседей новых поступивших точек
    private List<DbscanPoint> allPoints = new ArrayList<>();

    public Dbscan(int minNeighboursCount, double eps) {
        this.minNeighboursCount = minNeighboursCount;
        this.eps = eps;
    }

    // возможно вместо этого переделать под clazz.newInstance();, где класс
    // предается в конструктор
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
                // TODO: назначать id кластера
//                point.setVisited(true);
                //
                // TODO: так пока не получается, тк не все точки находятся сразу в allPoints
//                allPoints.add(point);
                List<DbscanPoint> neighbours = point.getNeighbours(allPoints, eps);
                // or
//                List<DbscanPoint> neighbours = point.getNeighbours(newPoints, eps);

                // TODO: здесь нужно проверять id кластеров всех соседей.
                // TODO: если у ВСЕХ UNVISITED, то создать новый кластер
                // TODO: если у ВСЕХ одинаковый id > 0, то эта точка принадлежит кластеру с этим id
                // TODO: если у ВСЕХ NOISE, то проверить количество и
                // TODO: возможно создать новый кластер со всеми этими соседями
                // TODO: непонятно, что делать, если разные id соседей (??)

                // перенести внутрь if ?
                if (neighbours.size() >= minNeighboursCount) {
                    if (checkNeighboursClusterId(neighbours)) {
                        // TODO: как-то определить кластер, в котором находится точка
                        int clusterId = neighbours.get(0).getClusterId();
                        point.setClusterId(clusterId);
                        clustering.findClusterById(clusterId).assignPoint((T) point);
                    }
                    else {
                        C newCluster = addCluster(lastClusterId);
                        newCluster.assignPoint((T) point);
                        point.setClusterId(lastClusterId);
                        while (!neighbours.isEmpty()) {
                            DbscanPoint lastNeighbour = neighbours.remove(neighbours.size() - 1);
                            if (!lastNeighbour.isVisited()) {
                                newCluster.assignPoint((T) lastNeighbour);
                                // TODO: назначать id кластера
                                // TODO: point or lastNeighbour?
                                //point.setVisited(true);
                                lastNeighbour.setClusterId(lastClusterId);
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
     * то есть точку, у которой этих соседей нашли нужно прицепить к этому кластеру
     * @param neighbours
     * @return
     */
    private boolean checkNeighboursClusterId(List<? extends DbscanPoint> neighbours) {
        int clusterId = neighbours.get(0).getClusterId();
        if (clusterId > DbscanPoint.UNVISITED) {
            for (int i = 1; i < neighbours.size(); i++) {
                if (clusterId != neighbours.get(i).getClusterId()) {
                    return false;
                }
            }
        }
        else return false;
        return true;
    }
}
