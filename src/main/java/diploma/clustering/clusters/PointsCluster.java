package diploma.clustering.clusters;

import diploma.clustering.Point;
import diploma.clustering.VectorOperations;
import diploma.clustering.dbscan.points.DbscanSimplePoint;

import java.util.List;

/**
 * @author Никита
 */
public class PointsCluster extends Cluster<DbscanSimplePoint> {
    private Double[] centerOfMass;
    private boolean wasUpdated;

    public PointsCluster() {
    }

//    public PointsCluster(int clusterId) {
//        super(clusterId);
//    }

    public PointsCluster(List<DbscanSimplePoint> points) {
        super(points);
    }

    @Override
    public void assignPoint(DbscanSimplePoint point) {
        super.assignPoint(point);
        this.wasUpdated = true;
    }

    public Double[] getCenterOfMass() {
        if (!wasUpdated) return centerOfMass;
        else {
            Double[] accumulator = getAssignedPoints().get(0).getCoordinatesVector();
            for (int i = 1; i < getAssignedPoints().size(); i++)
                accumulator = VectorOperations.addition(accumulator, getAssignedPoints().get(i).getCoordinatesVector());
            centerOfMass = VectorOperations.scalarMultiplication(accumulator, 1 / (double) getAssignedPoints().size());
            wasUpdated = false;
            return centerOfMass;
        }
    }
}
