package diploma.clustering.clusters;

import diploma.clustering.dbscan.points.DbscanClustersCluster;
import diploma.clustering.dbscan.points.DbscanPointsCluster;
import diploma.clustering.dbscan.points.DbscanPointsClustersCluster;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;

/**
 * @author Никита
 */
public class PointsClustersClustering extends Clustering<DbscanPointsClustersCluster, DbscanPointsCluster> {
    public PointsClustersClustering() {
        super();
    }

    public PointsClustersClustering(Double minSimilarity) {
        super(minSimilarity);
    }

    @Override
    public DbscanPointsClustersCluster findNearestCluster(DbscanPointsCluster point) {
        return null;
    }

    @Override
    public DbscanPointsClustersCluster createNewCluster() {
        return null;
    }
}
