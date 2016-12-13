package diploma.clustering.clusters;

import diploma.clustering.Point;

import java.util.List;

/**
 * @author Никита
 */
public class PointsCluster extends Cluster<Point> {
    public PointsCluster(int clusterId) {
        super(clusterId);
    }

    public PointsCluster(List<Point> points) {
        super(points);
    }
}
