package diploma.clustering.dbscan.points;

import diploma.clustering.Point;
import diploma.clustering.VectorOperations;
import diploma.clustering.clusters.PointsCluster;
import twitter4j.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class DbscanPointsCluster extends PointsCluster implements DbscanPoint, Serializable {
    private int clusterId = DbscanPoint.UNVISITED;

    public DbscanPointsCluster() {
        super();
    }

//    public DbscanPointsCluster(int clusterId, int clusterId1) {
//        super(clusterId);
//        this.clusterId = clusterId1;
//    }

    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> points, double eps) {
        List<DbscanPoint> neighbours = new ArrayList<>();
        for (DbscanPoint point: points) {
            if (point != this)
                if (VectorOperations.euclideanDistance(
                        ((DbscanPointsCluster) point).getCenterOfMass(), this.getCenterOfMass()) <= eps)
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
        // TODO: здесь нужно удалять все ассоциированные точки
        // TODO: при этом нужно сохранять id твита
        // TODO: Также, возможно, нужно удалять все из TfIdf, кроме tfIdfMapForAllDocuments
        this.clusterId = clusterId;
    }

    @Override
    public int getLastAssignedClusterId() {
        return 0;
    }
}
