package diploma.clustering.clusters;

import diploma.clustering.Point;

import java.util.List;

/**
 * @author Никита
 */
public class PointsCluster extends Cluster<Point> {
    public PointsCluster() {
    }

    public PointsCluster(List<Point> points) {
        super(points);
    }
}
