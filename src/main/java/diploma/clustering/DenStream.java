package diploma.clustering;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.clusters.StatusesClustering;
import diploma.clustering.dbscan.Dbscan;
import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;
import diploma.clustering.dbscan.points.SimplifiedDbscanStatusesCluster;
import diploma.clustering.dbscan.points.StatusDbscanPoint;
import diploma.statistics.MacroClusteringStatistics;
import diploma.statistics.dao.MacroClusteringStatisticsDao;
import diploma.statistics.dao.TweetDao;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Никита
 */
public class DenStream {
    private boolean initialized = true;
    private boolean withSubClustering = false;
    private List<StatusDbscanPoint> initBuffer = new ArrayList<>();
    private int minPoints;
    /* Минимальный размер микрокластера, для начала 10 - норм */
    private int mu;
    /* Коэффициент попадания в потенциальный микрокластер, для начала 5.0 - норм */
    private double beta;
    /* Чем больше lambda, тем меньше вес старых кластеров,
     * для начального значения норм около 0,0000001 */
    protected double lambda;
    private double initSimilarity;
    private double tp;
    private int numberOfProcessedUnits;
    private int statisticsCounter = 0;
    protected StatusesClustering outlierMicroClustering;
    protected StatusesClustering potentialMicroClustering;
    int executeCounter = 0;

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
            if (getPotentialMicroClustering().getClusters().size() != 0) {
                StatusesCluster nearestCluster = getPotentialMicroClustering().findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    if (withSubClustering) nearestCluster.assignPointToSubCluster(status);
                    merged = true;
                }
            }
            if (!merged && getOutlierMicroClustering().getClusters().size() != 0) {
                StatusesCluster nearestCluster = getOutlierMicroClustering().findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    if (withSubClustering) nearestCluster.assignPointToSubCluster(status);
                    merged = true;
                    if (nearestCluster.getWeight() > beta * mu) {
                        getOutlierMicroClustering().getClusters().remove(nearestCluster);
                        /* Меняем id кластера, тк среди потенциальных микрокластеров
                            уже может быть кластер с таким id */
                        nearestCluster.setId(getPotentialMicroClustering().getLastClusterId() + 1);
                        getPotentialMicroClustering().addCluster(nearestCluster);
                    }
                }
            }
            if (!merged) {
                StatusesCluster newCluster = new StatusesCluster(getOutlierMicroClustering().getLastClusterId() + 1, lambda);
                newCluster.assignPoint(status);
                getOutlierMicroClustering().addCluster(newCluster);
                if (withSubClustering) newCluster.createSubClustering(initSimilarity, mu, beta);
            }
        }
        if (System.currentTimeMillis() % tp == 0) {
            ArrayList<StatusesCluster> removalList = new ArrayList<>();
            for (StatusesCluster c : getPotentialMicroClustering().getClusters())
                if (c.getWeight() < beta * mu)
                    removalList.add(c);
                else if (withSubClustering) {
                    List<StatusesCluster> subClustersRemovalList = new ArrayList<>();
                    for (StatusesCluster subCluster : c.getPotentialSubClustering().getClusters())
                        if (subCluster.getWeight() < beta * mu) subClustersRemovalList.add(subCluster);
                    for (StatusesCluster subCluster : subClustersRemovalList)
                        c.getPotentialSubClustering().getClusters().remove(subCluster);

                    subClustersRemovalList.clear();
                    for (StatusesCluster subCluster : c.getOutlierSubClustering().getClusters()) {
                        long t0 = subCluster.getCreationTime();
                        double xsi1 = Math.pow(2, (-lambda * (System.currentTimeMillis() - t0 + tp))) - 1;
                        double xsi2 = Math.pow(2, -lambda * tp) - 1;
                        double xsi = xsi1 / xsi2;
                        if (subCluster.getWeight() < xsi)
                            removalList.add(c);
                    }
                    for (StatusesCluster subCluster : subClustersRemovalList)
                        c.getOutlierSubClustering().getClusters().remove(subCluster);
                }
            for (StatusesCluster c : removalList)
                getPotentialMicroClustering().getClusters().remove(c);

            removalList.clear();
            for (StatusesCluster c : getOutlierMicroClustering().getClusters()) {
                long t0 = c.getCreationTime();
                double xsi1 = Math.pow(2, (-lambda * (System.currentTimeMillis() - t0 + tp))) - 1;
                double xsi2 = Math.pow(2, -lambda * tp) - 1;
                double xsi = xsi1 / xsi2;
                if (c.getWeight() < xsi)
                    removalList.add(c);
            }
            for (StatusesCluster c : removalList)
                getOutlierMicroClustering().getClusters().remove(c);
        }
    }

    private void initialDbscan() {
        Dbscan dbscan = new Dbscan(minPoints, initSimilarity);
        dbscan.run(initBuffer);
        // создаем начальные кластера из того, что получилось в DBSCAN
        for (StatusDbscanPoint point: initBuffer) {
            if (!point.isNoise()) {
                if (getPotentialMicroClustering().findClusterById(point.getClusterId()) == null) {
                    StatusesCluster cluster = new StatusesCluster(point.getClusterId(), lambda);
                    cluster.assignPoint(point.getStatus());
                    getPotentialMicroClustering().addCluster(cluster);
                }
                else {
                    StatusesCluster cluster = getPotentialMicroClustering().findClusterById(point.getClusterId());
                    cluster.assignPoint(point.getStatus());
                }
            }
        }
    }

    protected MacroClusteringStatistics getClusterStatistics(Cluster<StatusesCluster> cluster, Timestamp time) {
        MacroClusteringStatistics statistics = new MacroClusteringStatistics();
        int totalNumberOfDocuments = 0;
        Map<String, Integer> topTenTerms = new HashMap<>();
        for (StatusesCluster statusesCluster: cluster.getAssignedPoints()) {
            totalNumberOfDocuments += statusesCluster.getTfIdf().getDocumentNumber();
            for (Map.Entry<String, Integer> entry: statusesCluster.getTfIdf().getTermFrequencyMap().entrySet())
                topTenTerms.merge(entry.getKey(), entry.getValue(), (num1, num2) -> num1 + num2);
        }
        topTenTerms = MapUtil.putFirstEntries(10, MapUtil.sortByValue(topTenTerms));
        statistics.setTimestamp(time);
        statistics.setTimeFactor(executeCounter);
        statistics.setClusterId(cluster.getId());
        statistics.setNumberOfDocuments(totalNumberOfDocuments);
        statistics.setTopTerms(topTenTerms);
        statistics.setAbsorbedClusterIds(cluster.getAbsorbedClusterIds());
        return statistics;
    }

}
