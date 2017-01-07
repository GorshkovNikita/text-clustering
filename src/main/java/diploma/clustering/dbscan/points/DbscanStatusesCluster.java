package diploma.clustering.dbscan.points;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.clusters.StatusesCluster;
import twitter4j.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Никита
 */
public class DbscanStatusesCluster extends StatusesCluster implements DbscanPoint, Serializable {
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
        Map<String, Integer> frequentTerms = new HashMap<>();
        for (DbscanPoint cluster: clusters) {
            int i = 0;
            for (Map.Entry<String, Integer> termWithItsFrequency: ((DbscanStatusesCluster) cluster).getTfIdf().getTermFrequencyMap().entrySet()) {
                if (frequentTerms.containsKey(termWithItsFrequency.getKey())) frequentTerms.put(termWithItsFrequency.getKey(), frequentTerms.get(termWithItsFrequency.getKey()) + 1);
                else frequentTerms.put(termWithItsFrequency.getKey(), 1);
                if (++i == 15) break;
            }
        }
        List<DbscanPoint> neighbours = new ArrayList<>();
        for (DbscanPoint cluster: clusters) {
            if (cluster != this)
                if (CosineSimilarity.cosineSimilarity(this.getTfIdf().getTfIdfForAllDocuments()
                                .entrySet().stream()
                                .filter(entry ->
                                        !frequentTerms.containsKey(entry.getKey()) ||
                                                (((double) frequentTerms.get(entry.getKey()) / (double) clusters.size() <= 0.2)))
                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())),
                        ((DbscanStatusesCluster) cluster).getTfIdf().getTfIdfForAllDocuments()
                                .entrySet().stream()
                                .filter(entry ->
                                        !frequentTerms.containsKey(entry.getKey()) ||
                                                (((double) frequentTerms.get(entry.getKey()) / (double) clusters.size() <= 0.2)))
                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()))) >= eps)
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
