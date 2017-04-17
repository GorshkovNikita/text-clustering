package diploma.clustering.dbscan.points;

import diploma.clustering.clusters.PointsClustersCluster;

import java.util.List;

/**
 * @author Никита
 */
public class DbscanPointsClustersCluster extends PointsClustersCluster implements DbscanPoint {
    /**
     * Идентификатор кластера, к которому элемент относится
     */
    private int clusterId = DbscanPoint.UNVISITED;

    public DbscanPointsClustersCluster() {
        super();
    }
//
//    public DbscanPointsClustersCluster(List<DbscanPoint> points) {
//        super((List<DbscanPointsCluster>)(List<?>)points);
//    }

//    public DbscanPointsClustersCluster(int clusterId) {
//        super(clusterId);
//    }

    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps) {
        return null;
    }

    @Override
    public void setNoise() {
        this.clusterId = NOISE;
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public int getLastAssignedClusterId() {
        return 0;
    }
}
