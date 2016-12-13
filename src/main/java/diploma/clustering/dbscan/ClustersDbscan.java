package diploma.clustering.dbscan;

import diploma.clustering.clusters.ClustersCluster;
import diploma.clustering.clusters.ClustersClustering;
import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.dbscan.points.DbscanClustersCluster;
import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Никита
 */
public class ClustersDbscan extends Dbscan<ClustersClustering, DbscanClustersCluster, DbscanStatusesCluster> {
    public ClustersDbscan(int minNeighboursCount, double eps) {
        super(minNeighboursCount, eps);
        this.clustering = new ClustersClustering();
    }

    public DbscanClustersCluster addCluster(int clusterId) {
        return new DbscanClustersCluster(clusterId);
    }
}
