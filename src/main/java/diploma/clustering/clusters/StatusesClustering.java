package diploma.clustering.clusters;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.TextNormalizer;
import diploma.clustering.dbscan.ClustersDbscan;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Никита
 */
public class StatusesClustering extends Clustering<DbscanStatusesCluster, Status> {
    @Override
    public DbscanStatusesCluster findNearestCluster(Status point) {
        String normalizedText = TextNormalizer.getInstance().normalizeToString(point.getText());
        DbscanStatusesCluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (DbscanStatusesCluster cluster: getClusters()) {
            Map<String, Double> tfIdfForAllDocuments = cluster.getTfIdf().getTfIdfForAllDocuments();
            Map<String, Double> tfIdfOfDocumentIntersection = cluster.getTfIdf().getTfIdfOfDocumentIntersection(normalizedText);
            Double similarity = CosineSimilarity.cosineSimilarity(tfIdfForAllDocuments, tfIdfOfDocumentIntersection);
            if (similarity > 0.1 && similarity > maxSimilarity) {
                nearestCluster = cluster;
                maxSimilarity = similarity;
            }
        }
        return nearestCluster;
    }

    @Override
    public DbscanStatusesCluster createNewCluster() {
        return new DbscanStatusesCluster();
    }

    public void process(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line = null;
            do {
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(line);
                        processNext(status);
                    } catch (TwitterException ignored) {}
                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public void processWithDbscan(Path filePath) {
        ClustersDbscan clustersDbscan = new ClustersDbscan(3, 0.1);
        int timestamp = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line = null;
            do {
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(line);
                        processNext(status);
                        timestamp++;
                    } catch (TwitterException ignored) {}
                }
                if (timestamp % 1000 == 0) {
                    clustersDbscan.run(getClusters());
                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StatusesClustering clustering = new StatusesClustering();
        clustering.process(Paths.get(Clustering.class.getClassLoader().getResource("2016-10-19-champions-league-first-1000.txt").getFile().substring(1)));
        System.out.println("Count of clusters = " + clustering.clusters.size());
    }
}
