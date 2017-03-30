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

import javax.lang.model.type.ArrayType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Никита
 */
public class DenStream {
    private boolean initialized = true;
    private List<StatusDbscanPoint> initBuffer = new ArrayList<>();
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
    private StatusesClustering outlierMicroClustering;
    private StatusesClustering potentialMicroClustering;

//    public DenStream() {}

    public DenStream(int minPoints, int mu, double beta, double lambda, double initSimilarity) {
        this.minPoints = minPoints;
        this.mu = mu;
        this.beta = beta;
        this.lambda = lambda;
        this.initSimilarity = initSimilarity;
        outlierMicroClustering = new StatusesClustering(initSimilarity);
        potentialMicroClustering = new StatusesClustering(initSimilarity);
        // для значений, описанных выше это равно 202028
        tp = Math.round(1 / lambda * Math.log((beta * mu) / (beta * mu - 1))) + 1;
    }

    public StatusesClustering getPotentialMicroClustering() {
        return potentialMicroClustering;
    }

    public StatusesClustering getOutlierMicroClustering() {
        return outlierMicroClustering;
    }

    public void processNext(EnhancedStatus status) {
        numberOfProcessedUnits++;
        if (!initialized) {
            initBuffer.add(new StatusDbscanPoint(status));
            if (initBuffer.size() >= 500) {
                initialDbscan();
                initialized = true;
            }
        }
        else {
            boolean merged = false;
            if (potentialMicroClustering.getClusters().size() != 0) {
                StatusesCluster nearestCluster = potentialMicroClustering.findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    merged = true;
                }
            }
            if (!merged && outlierMicroClustering.getClusters().size() != 0) {
                StatusesCluster nearestCluster = outlierMicroClustering.findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    merged = true;
                    if (nearestCluster.getWeight() > beta * mu) {
                        outlierMicroClustering.getClusters().remove(nearestCluster);
                    /* Меняем id кластера, тк среди потенциальных микрокластеров
                       уже может быть кластер с таким id */
                        nearestCluster.setId(potentialMicroClustering.getLastClusterId() + 1);
                        potentialMicroClustering.addCluster(nearestCluster);
                    }
                }
            }
            if (!merged) {
                StatusesCluster newCluster = new StatusesCluster(outlierMicroClustering.getLastClusterId() + 1, lambda);
                newCluster.assignPoint(status);
                outlierMicroClustering.addCluster(newCluster);
            }
        }
        if (System.currentTimeMillis() % tp == 0) {
            ArrayList<StatusesCluster> removalList = new ArrayList<>();
            for (StatusesCluster c : potentialMicroClustering.getClusters())
                if (c.getWeight() < beta * mu)
                    removalList.add(c);
            for (StatusesCluster c : removalList)
                potentialMicroClustering.getClusters().remove(c);

            for (StatusesCluster c : outlierMicroClustering.getClusters()) {
                long t0 = c.getCreationTime();
                double xsi1 = Math.pow(2, (-lambda * (System.currentTimeMillis() - t0 + tp))) - 1;
                double xsi2 = Math.pow(2, -lambda * tp) - 1;
                double xsi = xsi1 / xsi2;
                if (c.getWeight() < xsi)
                    removalList.add(c);
            }
            for (StatusesCluster c : removalList)
                outlierMicroClustering.getClusters().remove(c);
        }
    }

    private void initialDbscan() {
        Dbscan dbscan = new Dbscan(minPoints, initSimilarity);
        dbscan.run(initBuffer);
        // создаем начальные кластера из того, что получилось в DBSCAN
        for (StatusDbscanPoint point: initBuffer) {
            if (!point.isNoise()) {
                if (potentialMicroClustering.findClusterById(point.getClusterId()) == null) {
                    StatusesCluster cluster = new StatusesCluster(point.getClusterId(), lambda);
                    cluster.assignPoint(point.getStatus());
                    potentialMicroClustering.addCluster(cluster);
                }
                else {
                    StatusesCluster cluster = potentialMicroClustering.findClusterById(point.getClusterId());
                    cluster.assignPoint(point.getStatus());
                }
            }
        }
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
                        processNext(new EnhancedStatus(status));
                        timestamp++;
                    } catch (TwitterException ignored) {}
                }
                if (timestamp % 10000 == 0) {
                    List<DbscanStatusesCluster> list = new ArrayList<>();
                    for (StatusesCluster cluster : potentialMicroClustering.getClusters()) {
                        list.add(new DbscanStatusesCluster(cluster));
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
        DenStream denStream = new DenStream(10, 10, 5.0, 0.0000001, 0.4);
        List<EnhancedStatus> list = new ArrayList<>();
        try {
            EnhancedStatus s1 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
            EnhancedStatus s2 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump food london island\"}"));
            EnhancedStatus s3 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump brexit london tomorrow\"}"));
            EnhancedStatus s4 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump tweet joke iphone\"}"));
            EnhancedStatus s5 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump deal russia siria\"}"));
            list.add(s1); list.add(s2); list.add(s3); list.add(s4); list.add(s5);
//            EnhancedStatus s6 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
//            EnhancedStatus s7 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
//            EnhancedStatus s8 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
//            EnhancedStatus s9 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
//            EnhancedStatus s10 = new EnhancedStatus(TwitterObjectFactory.createStatus("{\"text\":\"trump meet somebody british\"}"));
        }
        catch (TwitterException ex) {

        }

        for (EnhancedStatus status : list) {
            denStream.processNext(status);
        }
//        denStream.processWithDbscan(Paths.get("D:\\MSU\\diploma\\tweets-sets\\full-random.txt"));
    }
}
