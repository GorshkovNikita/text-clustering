package diploma.clustering.clusters;

import diploma.clustering.dbscan.points.DbscanClustersCluster;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;

/**
 * Кластеризация микрокластеров
 * @author Никита
 */
public class ClustersClustering extends Clustering<DbscanClustersCluster, DbscanStatusesCluster> {
    @Override
    public DbscanClustersCluster findNearestCluster(DbscanStatusesCluster point) {
        return null;
    }

    @Override
    public DbscanClustersCluster createNewCluster() {
        return null;
    }
}
