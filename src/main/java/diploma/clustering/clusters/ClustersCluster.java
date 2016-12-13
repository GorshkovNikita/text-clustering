package diploma.clustering.clusters;

import diploma.clustering.dbscan.points.DbscanStatusesCluster;

import java.util.List;

/**
 * Кластер, элементами которого являются микрокластера твитов
 * @author Никита
 */
public class ClustersCluster extends Cluster<DbscanStatusesCluster> {
    public ClustersCluster(int clusterId) {
        super(clusterId);
    }

    public ClustersCluster(List<DbscanStatusesCluster> points) {
        super(points);
    }
}
