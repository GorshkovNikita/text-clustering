package diploma.clustering.clusters;

import diploma.clustering.dbscan.points.DbscanPointsCluster;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;

import java.util.List;

/**
 * @author Никита
 */
public class PointsClustersCluster extends Cluster<DbscanPointsCluster> {
    public PointsClustersCluster(int clusterId) {
        super(clusterId);
    }

    public PointsClustersCluster(List<DbscanPointsCluster> points) {
        super(points);
    }
}
