package diploma.clustering.dbscan;

import diploma.clustering.dbscan.points.DbscanSimplePoint;
import diploma.clustering.Point;
import diploma.clustering.clusters.PointsCluster;
import diploma.clustering.clusters.PointsClustering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class PointsDbscan extends Dbscan<PointsClustering, PointsCluster, Point> {
    public PointsDbscan(int minNeighboursCount, double eps) {
        super(minNeighboursCount, eps);
        this.clustering = new PointsClustering();
    }

    @Override
    public PointsCluster addCluster() {
        return new PointsCluster();
    }

    public static void main(String[] args) {
        List<DbscanSimplePoint> points = new ArrayList<>();
        PointsDbscan pointsDbscan = new PointsDbscan(2, 1.5);
        points.add(new DbscanSimplePoint(new Double[] {0.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {1.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {1.0, 1.0}));
        points.add(new DbscanSimplePoint(new Double[] {-2.0, -2.0}));
        points.add(new DbscanSimplePoint(new Double[] {-3.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[] {-2.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[] {-2.0, 5.0}));
        points.add(new DbscanSimplePoint(new Double[] {-1.0, 5.0}));
        points.add(new DbscanSimplePoint(new Double[] {-2.0, 6.0}));
        points.add(new DbscanSimplePoint(new Double[] {2.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {3.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {3.0, 1.0}));
        points.add(new DbscanSimplePoint(new Double[] {-5.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {-6.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[] {-3.0, 6.0}));
        points.add(new DbscanSimplePoint(new Double[] {3.0, 2.0}));
        pointsDbscan.run(points);
    }
}
