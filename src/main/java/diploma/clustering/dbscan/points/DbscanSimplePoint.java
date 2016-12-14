package diploma.clustering.dbscan.points;

import diploma.clustering.VectorOperations;
import diploma.clustering.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class DbscanSimplePoint extends Point implements DbscanPoint {
    private int clusterId;

    public DbscanSimplePoint(Double[] coordinatesVector) {
        super(coordinatesVector);
    }

    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps) {
        List<DbscanPoint> neighbours = new ArrayList<>();
        for (DbscanPoint point: points) {
            if (point != this)
                if (VectorOperations.euclideanDistance(((Point)point).getCoordinatesVector(), this.getCoordinatesVector()) < eps)
                    neighbours.add(point);
        }
        return neighbours;
    }

    @Override
    public void setNoise() {
        this.clusterId = NOISE;
    }

    @Override
    public int getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbscanSimplePoint)) return false;
        if (!super.equals(o)) return false;

//        DbscanSimplePoint that = (DbscanSimplePoint) o;
//
//        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result;
    }
}
