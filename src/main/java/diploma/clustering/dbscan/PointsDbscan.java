package diploma.clustering.dbscan;

import diploma.clustering.clusters.PointsClustersClustering;
import diploma.clustering.dbscan.points.DbscanPointsCluster;
import diploma.clustering.dbscan.points.DbscanPointsClustersCluster;
import diploma.clustering.dbscan.points.DbscanSimplePoint;
import diploma.clustering.Point;
import diploma.clustering.clusters.PointsCluster;
import diploma.clustering.clusters.PointsClustering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class PointsDbscan extends Dbscan<PointsClustersClustering, DbscanPointsClustersCluster, DbscanPointsCluster> {
    public PointsDbscan(int minNeighboursCount, double eps) {
        super(minNeighboursCount, eps);
        this.clustering = new PointsClustersClustering();
    }

    @Override
    public DbscanPointsClustersCluster addCluster(int clusterId) {
        return new DbscanPointsClustersCluster(clusterId);
    }

    public static void main(String[] args) {
        // Тест 1
//        List<DbscanSimplePoint> points = new ArrayList<>();
//        PointsDbscan pointsDbscan = new PointsDbscan(2, 1.5);
//        points.add(new DbscanSimplePoint(new Double[] {0.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {1.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {1.0, 1.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-2.0, -2.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-3.0, 4.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-2.0, 4.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-2.0, 5.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-1.0, 5.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-2.0, 6.0}));
//        points.add(new DbscanSimplePoint(new Double[] {2.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {3.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {2.0, 1.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-5.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-6.0, 0.0}));
//        points.add(new DbscanSimplePoint(new Double[] {-3.0, 6.0}));
//        points.add(new DbscanSimplePoint(new Double[] {3.0, 2.0}));
//        pointsDbscan.run(points);
//        List<DbscanSimplePoint> newPoints = new ArrayList<>();
//        newPoints.add(new DbscanSimplePoint(new Double[] {3.0, 1.0}));
//        newPoints.add(new DbscanSimplePoint(new Double[] {-5.0, -1.0}));
//        newPoints.add(new DbscanSimplePoint(new Double[] {-6.0, -1.0}));
//        newPoints.add(new DbscanSimplePoint(new Double[] {-1.0, 4.0}));
//        newPoints.add(new DbscanSimplePoint(new Double[] {0.0, 3.0}));
//        pointsDbscan.run(newPoints);
//        List<DbscanSimplePoint> newPoints2 = new ArrayList<>();
//        newPoints2.add(new DbscanSimplePoint(new Double[] {1.0, 2.0}));
//        newPoints2.add(new DbscanSimplePoint(new Double[] {-4.0, 0.0}));
//        newPoints2.add(new DbscanSimplePoint(new Double[] {-3.0, 0.0}));
//        newPoints2.add(new DbscanSimplePoint(new Double[] {-2.0, 0.0}));
//        newPoints2.add(new DbscanSimplePoint(new Double[] {-1.0, 0.0}));
//        pointsDbscan.run(newPoints2);
//
        // Тест 2
        List<DbscanSimplePoint> points = new ArrayList<>();
        PointsDbscan pointsDbscan = new PointsDbscan(3, 1.5);
        // Кластер 1
        points.add(new DbscanSimplePoint(new Double[]{-1.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[]{-2.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[]{-3.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[]{-3.0, 3.0}));
        points.add(new DbscanSimplePoint(new Double[]{-4.0, 3.0}));
        points.add(new DbscanSimplePoint(new Double[]{-4.0, 4.0}));
        // Кластер 2
        points.add(new DbscanSimplePoint(new Double[]{1.0, 3.0}));
        points.add(new DbscanSimplePoint(new Double[]{1.0, 4.0}));
        points.add(new DbscanSimplePoint(new Double[]{2.0, 3.0}));
        points.add(new DbscanSimplePoint(new Double[]{2.0, 4.0}));
        // Шум
        points.add(new DbscanSimplePoint(new Double[]{1.0, 0.0}));
        points.add(new DbscanSimplePoint(new Double[]{0.0, 0.0}));
        pointsDbscan.run(points);

        points.clear();
        // Создание 3 кластера из шума
        points.add(new DbscanSimplePoint(new Double[]{0.0, -1.0}));
        points.add(new DbscanSimplePoint(new Double[]{1.0, -1.0}));
        points.add(new DbscanSimplePoint(new Double[]{2.0, -1.0}));
        pointsDbscan.run(points);

        points.clear();
        // Слияние 2го и 3го кластеров
        points.add(new DbscanSimplePoint(new Double[]{1.0, 1.0}));
        points.add(new DbscanSimplePoint(new Double[]{1.0, 2.0}));
        points.add(new DbscanSimplePoint(new Double[]{-1.0, 3.0}));
        pointsDbscan.run(points);
    }
}
