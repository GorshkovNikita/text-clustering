package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.MapUtil;
import diploma.clustering.VectorOperations;
import diploma.clustering.dbscan.points.DbscanPointsCluster;
import diploma.clustering.dbscan.points.DbscanSimplePoint;

/**
 * @author Никита
 */
public class PointsClustering extends Clustering<DbscanPointsCluster, DbscanSimplePoint> {
    public PointsClustering() {
        super();
    }

    public PointsClustering(Double minSimilarity) {
        super(minSimilarity);
    }

//    @Override
//    public DbscanPointsCluster createNewCluster() {
//        return new DbscanPointsCluster();
//    }

    @Override
    public DbscanPointsCluster findNearestCluster(DbscanSimplePoint point) {
        DbscanPointsCluster nearestCluster = null;
        Double minDistance = Double.MAX_VALUE;
        for (DbscanPointsCluster pointsCluster : getClusters()) {
            Double distance = VectorOperations.euclideanDistance(point.getCoordinatesVector(), pointsCluster.getCenterOfMass());
//            System.out.println(distance);
            if (distance < minDistance && distance < minSimilarity) {
                nearestCluster = pointsCluster;
                minDistance = distance;
            }
        }
        return nearestCluster;
    }
}
