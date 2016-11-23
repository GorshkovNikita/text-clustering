package diploma.clustering.clusters;

import java.util.List;

/**
 * Кластер, элементами которого являются микрокластера твитов
 * @author Никита
 */
public class ClustersCluster extends Cluster<StatusesCluster> {
    public ClustersCluster() {
    }

    public ClustersCluster(List<StatusesCluster> points) {
        super(points);
    }
}
