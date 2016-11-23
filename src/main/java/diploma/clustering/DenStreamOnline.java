package diploma.clustering;

import diploma.clustering.clusters.StatusesClustering;

import java.nio.file.Paths;

/**
 * @author Никита
 */
public class DenStreamOnline {
    public static void main(String[] args) {
        StatusesClustering statusesClustering = new StatusesClustering();
//        statusesClustering.processWithDbscan(Paths.get(Clustering.class.getClassLoader().getResource("2016-10-19-champions-league-first-1000.txt").getFile().substring(1)));
        statusesClustering.processWithDbscan(Paths.get("D:\\MSU\\diploma\\tweets-sets\\2016-10-19-champions-league\\2016-10-19-champions-league-full-json-first-50000.txt"));
    }
}
