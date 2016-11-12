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

/**
 * @author Никита
 */
public class Clustering {
    private static List<Cluster> clusters = new ArrayList<>();
    private static TextNormalizer normalizer = new TextNormalizer();

    private static class Cluster {
        private TfIdf tfIdf;
        private List<Status> statuses;

        public Cluster() {
            tfIdf = new TfIdf();
            statuses = new ArrayList<>();
        }

        public void assignStatus(Status status) {
            statuses.add(status);
            tfIdf.updateForNewDocument(Long.toString(status.getId()), normalizer.normalizeToString(status.getText()));
        }

        public List<Status> getStatuses() {
            return statuses;
        }

        public TfIdf getTfIdf() {
            return tfIdf;
        }
    }

    public static Cluster findNearestCluster(String normalizedText) {
        Cluster nearestCluster = null;
        Double maxSimilarity = 0.0;
        for (Cluster cluster: clusters) {
            Double similarity = CosineSimilarity.cosineSimilarity(
                    cluster.getTfIdf().getAugmentedTfIdfForAllDocuments(normalizedText),
                    cluster.getTfIdf().getTfIdfForSpecificDocumentWithContent(normalizedText));
            if (similarity > 0.003 && similarity > maxSimilarity) {
                nearestCluster = cluster;
                maxSimilarity = similarity;
            }
        }
        return nearestCluster;
    }

    public static void process(Path filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String line1 = null;
            int i = 0;
            do {
                line1 = br.readLine();
                if (line1 != null && !line1.equals("")) {
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(line1);
                    }
                    catch (TwitterException ignored) {}
                    String normalizedText = normalizer.normalizeToString(status.getText());
                    Cluster nearestCluster = findNearestCluster(normalizedText);
                    if (nearestCluster == null) {
                        Cluster newCluster = new Cluster();
                        newCluster.assignStatus(status);
                        clusters.add(newCluster);
                    }
                    else nearestCluster.assignStatus(status);
                }
            } while (line1 != null);
            List<Cluster> bigClusters = new ArrayList<>();
            for (Cluster cluster: clusters) {
                if (cluster.getStatuses().size() >= 5) {
                    bigClusters.add(cluster);
                }
            }
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        process(Paths.get(Clustering.class.getClassLoader().getResource("2016-10-19-champions-league-first-1000.txt").getFile().substring(1)));
    }
}
