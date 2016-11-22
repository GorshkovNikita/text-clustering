package diploma.clustering.tfidf;

import diploma.clustering.CosineSimilarity;
import diploma.clustering.MapUtil;
import diploma.clustering.TextNormalizer;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Никита
 */
public class Clustering {
    private List<Cluster> clusters = new ArrayList<>();

    public Cluster findNearestCluster(String normalizedText) {
        Cluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (Cluster cluster: clusters) {
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

    public void process(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line = null;
            int i = 0;
            do {
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(line);
                    }
                    catch (TwitterException ignored) {}
                    String normalizedText = TextNormalizer.getInstance().normalizeToString(status.getText());
                    if (normalizedText.split(" ").length >= 4) {
                        Cluster nearestCluster = findNearestCluster(normalizedText);
                        if (nearestCluster == null) {
                            Cluster newCluster = new Cluster();
                            newCluster.assignStatus(status);
                            clusters.add(newCluster);
                        } else nearestCluster.assignStatus(status);
                    }
                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Clustering clustering = new Clustering();
        clustering.process(Paths.get(Clustering.class.getClassLoader().getResource("2016-10-19-champions-league-first-1000.txt").getFile().substring(1)));
        System.out.println("Count of clusters = " + clustering.clusters.size());
    }
}
