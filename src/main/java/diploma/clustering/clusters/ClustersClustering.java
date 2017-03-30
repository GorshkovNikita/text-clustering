package diploma.clustering.clusters;

import diploma.clustering.dbscan.points.DbscanClustersCluster;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;

/**
 * Кластеризация микрокластеров
 * @author Никита
 */
public class ClustersClustering extends Clustering<DbscanClustersCluster, DbscanStatusesCluster> {
    public ClustersClustering() {
        super();
    }

    public ClustersClustering(Double minSimilarity) {
        super(minSimilarity);
    }

    @Override
    public DbscanClustersCluster findNearestCluster(DbscanStatusesCluster point) {
        return null;
    }

//    @Override
//    public DbscanClustersCluster createNewCluster() {
//        return null;
//    }

    public static void main(String[] args) {

    }
}
