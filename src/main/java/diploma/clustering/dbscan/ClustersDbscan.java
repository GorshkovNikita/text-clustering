package diploma.clustering.dbscan;

import diploma.clustering.clusters.ClustersCluster;
import diploma.clustering.clusters.ClustersClustering;
import diploma.clustering.clusters.StatusesCluster;

/**
 * @author Никита
 */
public class ClustersDbscan extends Dbscan<ClustersClustering, ClustersCluster, StatusesCluster> {
    public ClustersDbscan(int minNeighboursCount, double eps) {
        super(minNeighboursCount, eps);
        this.clustering = new ClustersClustering();
    }

    public ClustersCluster addCluster() {
        return new ClustersCluster();
    }
}
