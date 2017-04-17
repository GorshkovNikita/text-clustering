package diploma.clustering.dbscan.points;

import diploma.clustering.MapUtil;
import diploma.clustering.clusters.StatusesCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Упрощенный поиск соседей для DBSCAN алгоритма с помощью общих ключевых слов
 * @author Никита
 */
public class SimplifiedDbscanStatusesCluster extends DbscanStatusesCluster {
    public SimplifiedDbscanStatusesCluster() {}

    public SimplifiedDbscanStatusesCluster(StatusesCluster statusesCluster, int lastAssignedClusterId) {
        super(statusesCluster, lastAssignedClusterId);
    }

    @Override
    public List<DbscanStatusesCluster> getNeighbours(List<? extends DbscanPoint> clusters, double eps) {
        List<DbscanStatusesCluster> neighbours = new ArrayList<>();
        for (DbscanPoint cluster: clusters) {
            if (cluster != this && hasCommonTopTerms(
                    ((DbscanStatusesCluster) cluster).getStatusesCluster().getTfIdf().getTermFrequencyMap(),
                    this.getStatusesCluster().getTfIdf().getTermFrequencyMap()))
                neighbours.add((DbscanStatusesCluster) cluster);
        }
        return neighbours;
    }

    boolean hasCommonTopTerms(Map<String, Integer> firstClusterTermFrequencyMap, Map<String, Integer> secondClusterTermFrequencyMap) {
        firstClusterTermFrequencyMap = MapUtil.sortByValue(firstClusterTermFrequencyMap).entrySet().stream().limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        secondClusterTermFrequencyMap = MapUtil.sortByValue(secondClusterTermFrequencyMap).entrySet().stream().limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        int numberOfCommonTerms = 0;
        for (String term : firstClusterTermFrequencyMap.keySet()) {
            if (secondClusterTermFrequencyMap.containsKey(term)) numberOfCommonTerms++;
            if (numberOfCommonTerms >= 4) return true;
        }
        return false;
    }
}
