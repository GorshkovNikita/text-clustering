package diploma.clustering.dbscan.points;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.clusters.StatusesCluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class DbscanStatusesCluster implements DbscanPoint, Serializable {
    private int clusterId = DbscanPoint.UNVISITED;
    private int lastAssignedClusterId = 0;
    private StatusesCluster statusesCluster;

    public DbscanStatusesCluster() {}

//    public DbscanStatusesCluster(int clusterId, int clusterId1) {
//        super(clusterId);
//        this.clusterId = clusterId1;
//    }

    public DbscanStatusesCluster(StatusesCluster statusesCluster, int lastAssignedClusterId) {
        this.statusesCluster = statusesCluster;
        this.lastAssignedClusterId = lastAssignedClusterId;
    }

    /**
     * Поиск соседей с помощью косинуса угла между векторами tf-idf текущего кластера и всех остальных
     * TODO: подумать, как это сделать эффективно, так как количество кластеров может быть очень большим
     * @param clusters - кластера, из которых нужно найти соседние
     * @param eps - порог, по которому определяется является ли точка соседом или нет,
     *            в данном случае - это значение косинуса угла
     * @return - соседи
     */
    @Override
    public List<DbscanStatusesCluster> getNeighbours(List<? extends DbscanPoint> clusters, double eps) {
//        Map<String, Integer> frequentTerms = new HashMap<>();
//        for (DbscanPoint cluster: clusters) {
//            int i = 0;
//            for (Map.Entry<String, Integer> termWithItsFrequency: ((DbscanStatusesCluster)cluster).getStatusesCluster().getTfIdf().getTermFrequencyMap().entrySet()) {
//                if (frequentTerms.containsKey(termWithItsFrequency.getKey())) frequentTerms.put(termWithItsFrequency.getKey(), frequentTerms.get(termWithItsFrequency.getKey()) + 1);
//                else frequentTerms.put(termWithItsFrequency.getKey(), 1);
//                if (++i == 15) break;
//            }
//        }
        List<DbscanStatusesCluster> neighbours = new ArrayList<>();
        for (DbscanPoint cluster: clusters) {
            if (cluster != this)
                if (CosineSimilarity.cosineSimilarity(
                        statusesCluster.getTfIdf().getTfIdfForAllDocuments(),
                        ((DbscanStatusesCluster) cluster).getStatusesCluster().getTfIdf().getTfIdfForAllDocuments()
//                        filterTfIdfByFrequentTerms(statusesCluster.getTfIdf().getTfIdfForAllDocuments(), frequentTerms, clusters.size()).entrySet().stream()
//                                .limit(100)
//                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())),
//                        filterTfIdfByFrequentTerms(((DbscanStatusesCluster) cluster).getStatusesCluster().getTfIdf().getTfIdfForAllDocuments(), frequentTerms, clusters.size()).entrySet().stream()
//                                .limit(100)
//                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()))
                )
                        >= eps)
                    neighbours.add((DbscanStatusesCluster) cluster);
        }
        return neighbours;
    }

//    @Override
//    public void assignPoint(Status point) {
//        super.assignPoint(point);
//    }

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
        return lastAssignedClusterId;
    }

    //    /**
//     * Фильтрация вектора tf-idf часто встречающимися термами.
//     * @param tfIdfVector - фильтруемый вектор tf-idf
//     * @param frequentTerms - часто встречающиеся термы
//     * @param numberOfClusters - количество кластеров
//     * @return - отфильтрованный вектор tf-idf
//     */
//    private Map<String, Double> filterTfIdfByFrequentTerms(Map<String, Double> tfIdfVector, Map<String, Integer> frequentTerms, int numberOfClusters) {
//        return tfIdfVector.entrySet().stream()
//                .filter(entry ->
//                        // пропускает термы, не входящие в карту частых термов
//                        !frequentTerms.containsKey(entry.getKey()) ||
//                                // или термы, которые встречаются реже, чем в каждом 5-ом кластере
//                                (((double) frequentTerms.get(entry.getKey()) / (double) numberOfClusters <= 0.2)))
//                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
//    }

    public StatusesCluster getStatusesCluster() {
        return statusesCluster;
    }
}
