package diploma.clustering.dbscan.points;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.clusters.StatusesCluster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class DbscanStatusesCluster extends StatusesCluster implements DbscanPoint {
    private boolean isNoise = false;
    private boolean isVisited = false;

    @Override
    public List<DbscanPoint> getNeighbours(List<? extends DbscanPoint> clusters, double eps) {
        List<DbscanPoint> neighbours = new ArrayList<>();
        for (DbscanPoint cluster: clusters) {
            if (cluster != this)
                if (CosineSimilarity.cosineSimilarity(this.getTfIdf().getTfIdfForAllDocuments(), ((DbscanStatusesCluster) cluster).getTfIdf().getTfIdfForAllDocuments()) >= eps)
                    neighbours.add(cluster);
        }
        return neighbours;
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
