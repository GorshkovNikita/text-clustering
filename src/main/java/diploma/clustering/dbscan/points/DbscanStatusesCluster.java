package diploma.clustering.dbscan.points;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.clusters.StatusesCluster;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class DbscanStatusesCluster extends StatusesCluster implements DbscanPoint {
    private int clusterId = DbscanPoint.UNVISITED;

    public DbscanStatusesCluster() {
        super();
    }

    public DbscanStatusesCluster(int clusterId, int clusterId1) {
        super(clusterId);
        this.clusterId = clusterId1;
    }

    // TODO: подумать, как это сделать эффективно, так как количество кластеров может быть очень большим
    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> clusters, double eps) {
        List<DbscanPoint> neighbours = new ArrayList<>();
        for (DbscanPoint cluster: clusters) {
            if (cluster != this)
                if (CosineSimilarity.cosineSimilarity(this.getTfIdf().getTfIdfForAllDocuments(),
                        ((DbscanStatusesCluster) cluster).getTfIdf().getTfIdfForAllDocuments()) >= eps)
                    neighbours.add(cluster);
        }
        return neighbours;
    }

    @Override
    public void assignPoint(Status point) {
        super.assignPoint(point);
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
}
