package diploma.clustering;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.clusters.StatusesClustering;
import diploma.clustering.dbscan.Dbscan;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;
import diploma.clustering.dbscan.points.StatusDbscanPoint;
import twitter4j.Status;
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
public class MyDenStream {
    private int minPoints;
    /* Минимальный размер микрокластера, для начала 10 - норм */
    private int mu;
    /* Коэффициент попадания в потенциальный микрокластер, для начала 5.0 - норм */
    private double beta;
    /* Чем больше lambda, тем меньше вес старых кластеров,
     * для начального значения норм около 0,0000001 */
    private double lambda;
    private double initSimilarity;
    private double tp;
    private int numberOfProcessedUnits;
    private StatusesClustering outlierMicroClustering = new StatusesClustering(0.2);
    private StatusesClustering potentialMicroClustering = new StatusesClustering(0.2);

//    public DenStream() {}

    public MyDenStream(int minPoints, int mu, double beta, double lambda, double initSimilarity) {
        this.minPoints = minPoints;
        this.mu = mu;
        this.beta = beta;
        this.lambda = lambda;
        this.initSimilarity = initSimilarity;
        // для значений, описанных выше это равно 202028
        tp = Math.round(1 / lambda * Math.log((beta * mu) / (beta * mu - 1))) + 1;
    }

    public StatusesClustering getPotentialMicroClustering() {
        return potentialMicroClustering;
    }

    public StatusesClustering getOutlierMicroClustering() {
        return outlierMicroClustering;
    }

    public void processNext(Status status) {
//        numberOfProcessedUnits++;
//        StatusesCluster nearestCluster = findNearestCluster(point);
//        if (nearestCluster == null) {
//            C newCluster = createNewCluster();
//            newCluster.assignPoint(point);
//            newCluster.setLastUpdateTime(timestamp);
//            addCluster(newCluster);
//        } else {
//            nearestCluster.assignPoint(point);
//            nearestCluster.setLastUpdateTime(timestamp);
//        }
    }

    public void processWithDbscan(Path filePath) {
        Dbscan dbscan = new Dbscan(3, 0.4);
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
                if (timestamp % 10000 == 0) {
                    List<DbscanStatusesCluster> list = new ArrayList<>();
                    for (StatusesCluster cluster : potentialMicroClustering.getClusters()) {
                        list.add(new DbscanStatusesCluster(cluster, 0));
                    }
                    dbscan.run(list);
                    Clustering<Cluster<StatusesCluster>, StatusesCluster> macroClustering = new Clustering<>();
                    for (DbscanStatusesCluster point: list) {
                        if (macroClustering.findClusterById(point.getClusterId()) == null) {
                            Cluster<StatusesCluster> cluster = new Cluster<>(point.getClusterId(), 0.00001);
                            cluster.assignPoint(point.getStatusesCluster());
                            macroClustering.addCluster(cluster);
                        }
                        else {
                            Cluster<StatusesCluster> cluster = macroClustering.findClusterById(point.getClusterId());
                            cluster.assignPoint(point.getStatusesCluster());
                        }
                    }
                    System.out.println(macroClustering.toString());
                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MyDenStream denStream = new MyDenStream(10, 10, 5.0, 0.0000001, 0.4);
        denStream.processWithDbscan(Paths.get("D:\\MSU\\diploma\\tweets-sets\\full-random.txt"));
    }
}
