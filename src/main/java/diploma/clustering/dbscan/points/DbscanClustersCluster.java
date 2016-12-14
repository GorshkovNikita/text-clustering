package diploma.clustering.dbscan.points;

import diploma.clustering.clusters.ClustersCluster;
import diploma.clustering.clusters.StatusesCluster;

import java.util.List;

/**
 * @author Никита
 */
public class DbscanClustersCluster extends ClustersCluster implements DbscanPoint {
    /**
     * Идентификатор кластера, к которому элемент относится
     */
    private int clusterId = DbscanPoint.UNVISITED;

    public DbscanClustersCluster(List<DbscanPoint> points) {
        super((List<DbscanStatusesCluster>)(List<?>)points);
    }

    public DbscanClustersCluster(int clusterId) {
        super(clusterId);
    }

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
}
