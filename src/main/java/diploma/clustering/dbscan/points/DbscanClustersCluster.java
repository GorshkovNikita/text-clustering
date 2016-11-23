package diploma.clustering.dbscan.points;

import diploma.clustering.clusters.ClustersCluster;
import diploma.clustering.clusters.StatusesCluster;

import java.util.List;

/**
 * @author Никита
 */
public class DbscanClustersCluster extends ClustersCluster implements DbscanPoint {
    private boolean isNoise = false;
    private boolean isVisited = false;

    public DbscanClustersCluster(List<DbscanPoint> points) {
        super((List<StatusesCluster>)(List<?>)points);
    }

    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps) {
        return null;
    }

    @Override
    public void setNoise(boolean isNoise) {
        this.isNoise = isNoise;
    }

    @Override
    public boolean isNoise() {
        return isNoise;
    }

    @Override
    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    @Override
    public boolean isVisited() {
        return isVisited;
    }
}
