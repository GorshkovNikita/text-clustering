package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.EnhancedStatus;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Никита
 */
public class StatusesClustering extends Clustering<StatusesCluster, EnhancedStatus> {
    public StatusesClustering() {
        super();
    }

    public StatusesClustering(Double minSimilarity) {
        super(minSimilarity);
    }

    @Override
    public StatusesCluster findNearestCluster(EnhancedStatus point) {
        StatusesCluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (StatusesCluster cluster: getClusters()) {
            Map<String, Double> tfIdfForAllDocuments = cluster.getTfIdf().getTfIdfForAllDocuments();
            Map<String, Double> tfIdfOfDocumentIntersection = cluster.getTfIdf().getTfIdfOfDocumentIntersection(point.getNormalizedText());
            Double similarity = CosineSimilarity.cosineSimilarity(tfIdfForAllDocuments, tfIdfOfDocumentIntersection);
            if (similarity > minSimilarity && similarity > maxSimilarity) {
                nearestCluster = cluster;
                maxSimilarity = similarity;
            }
        }
        return nearestCluster;
    }

//    @Override
//    public StatusesCluster createNewCluster() {
//        return new StatusesCluster();
//    }

//    public void updateFrequentTerms() {
//
//    }

//    public void process(Path filePath) {
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
//            String line = null;
//            do {
//                line = br.readLine();
//                if (line != null && !line.equals("")) {
//                    Status status = null;
//                    try {
//                        status = TwitterObjectFactory.createStatus(line);
////                        processNext(status);
//                    } catch (TwitterException ignored) {}
//                }
//            } while (line != null);
//            br.close();
//        }
//        catch (IOException | IllegalArgumentException ex) {
//            ex.printStackTrace();
//        }
//    }

    /**
     * Для тестовых целей НЕ в Apache Storm
     * @param filePath - путь к файлу с твитами
     */
    public void processWithDbscan(Path filePath) {
//        ClustersDbscan clustersDbscan = new ClustersDbscan(3, 0.4);
//        int timestamp = 0;
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
//            String line = null;
//            do {
//                line = br.readLine();
//                if (line != null && !line.equals("")) {
//                    Status status = null;
//                    try {
//                        status = TwitterObjectFactory.createStatus(line);
//                        processNext(status);
//                        timestamp++;
//                    } catch (TwitterException ignored) {}
//                }
//                if (timestamp % 10000 == 0) {
//                    // TODO: после отправления точек в DBSCAN желательно их удалять, тк
//                    // TODO: этот метод будет выполнятся на каждом болте, а следовательно,
//                    // TODO: если сохранять все точки, то это будет занимать много места
//                    // TODO: это альтернатива варианту с id кластеров
//
//                    // TODO: для использования в Storm видимо нужно копировать коллекцию, потом очищать ее в болте,
//                    // TODO: а копию отправлять в следующий spout
//                    List<DbscanStatusesCluster> bigClusters = getClusters()
//                            .stream()
//                            .filter((cluster) -> cluster.getAssignedPoints().size() > MIN_POINTS)
//                                    // Если дополнительно для фильтра использовать: && !cluster.isVisited()), то
//                                    // тогда нельзя будет найти всех соседей
//                            .collect(Collectors.toList());
//                    for (DbscanStatusesCluster cluster: bigClusters) {
//                        cluster.getTfIdf().sortTermFrequencyMap();
//                    }
//                    clustersDbscan.run(bigClusters);
//                    for (DbscanStatusesCluster cluster: bigClusters) {
//                        getClusters().remove(cluster);
//                    }
//                }
//            } while (line != null);
//            br.close();
//        }
//        catch (IOException | IllegalArgumentException ex) {
//            ex.printStackTrace();
//        }
    }

//    public static void main(String[] args) {
//        StatusesClustering clustering = new StatusesClustering(0.2);
//        clustering.process(Paths.get(Clustering.class.getClassLoader().getResource("2016-10-19-champions-league-first-1000.txt").getFile().substring(1)));
//        System.out.println("Count of clusters = " + clustering.clusters.size());
//    }
}
