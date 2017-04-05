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
    private double lambda;
    private double initSimilarity;
    private double tp;
    private int numberOfProcessedUnits;
    private int statisticsCounter = 0;
    private StatusesClustering outlierMicroClustering;
    private StatusesClustering potentialMicroClustering;
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
            if (potentialMicroClustering.getClusters().size() != 0) {
                StatusesCluster nearestCluster = potentialMicroClustering.findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    if (withSubClustering) nearestCluster.assignPointToSubCluster(status);
                    merged = true;
                }
            }
            if (!merged && outlierMicroClustering.getClusters().size() != 0) {
                StatusesCluster nearestCluster = outlierMicroClustering.findNearestCluster(status);
                if (nearestCluster != null) {
                    nearestCluster.assignPoint(status);
                    if (withSubClustering) nearestCluster.assignPointToSubCluster(status);
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
                if (withSubClustering) newCluster.createSubClustering(initSimilarity, mu, beta);
            }
        }
        if (System.currentTimeMillis() % tp == 0) {
            ArrayList<StatusesCluster> removalList = new ArrayList<>();
            for (StatusesCluster c : potentialMicroClustering.getClusters())
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
                potentialMicroClustering.getClusters().remove(c);

            removalList.clear();
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

    private MacroClusteringStatistics getClusterStatistics(Cluster<StatusesCluster> cluster, Timestamp time) {
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

    /**
     * Тест алгоритма без apache storm
     * @param args
     */
    public static void main(String[] args) {
//        List<StatusesCluster> potentialClusters = new ArrayList<>();
//        StatefulDbscan statefulDbscan = new StatefulDbscan(3, 0.6);
        Dbscan dbscan = new Dbscan(0, 0.6);
        MacroClusteringStatisticsDao statisticsDao = new MacroClusteringStatisticsDao();
        int numberOfDocuments = 0;
        DenStream denStream = new DenStream(10, 10, 3.0, -Math.log(3.0) / Math.log(2)/(double) 400, 0.2);

        try (BufferedReader br = new BufferedReader(new FileReader("D:\\MSU\\diploma\\tweets-sets\\2017-04-02-sport-events.txt"))) {
            String line = null;
            do {
                long start = System.currentTimeMillis();
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    EnhancedStatus status = null;
                    try {
                        numberOfDocuments++;
                        status = new EnhancedStatus(TwitterObjectFactory.createStatus(line));
                        if (!"".equals(status.getNormalizedText()))
                            denStream.processNext(status);
                    } catch (TwitterException ignored) {}
                }
//                if (numberOfDocuments % 300 == 0) {
//                    for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters()) {
//                        cluster.getTfIdf().sortTermFrequencyMap();
//                        cluster.getTfIdf().limitTermFrequencyMap(25);
////                        collector.emit(new Values(cluster));
//                        potentialClusters.add(cluster);
//                    }
//                    Collections.sort(denStream.getOutlierMicroClustering().getClusters(), new Comparator<StatusesCluster>() {
//                        @Override
//                        public int compare(final StatusesCluster object1, final StatusesCluster object2) {
//                            return ((Integer) object2.getSize()).compareTo(object1.getSize());
//                        }
//                    });
//                    denStream.getPotentialMicroClustering().getClusters().clear();
//                }

                if (numberOfDocuments % 3000 == 0) {
                    List<DbscanStatusesCluster> incomingPoints = new ArrayList<>();
                    for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters())
                        incomingPoints.add(new SimplifiedDbscanStatusesCluster(cluster));

                    // удаляем старые микрокластера
//                    List<DbscanPoint> removalList = new ArrayList<>();
//                    for (DbscanPoint statusesCluster : statefulDbscan.getAllPoints())
//                        // каждый час
//                        if ((System.currentTimeMillis() - ((DbscanStatusesCluster)statusesCluster).getStatusesCluster().getLastUpdateTime()) > 3600 * 1000)
//                            removalList.add(statusesCluster);
//                    for (DbscanPoint statusesCluster : removalList)
//                        statefulDbscan.getAllPoints().remove(statusesCluster);

                    dbscan.run(incomingPoints);
//                    potentialClusters.clear();

                    // сбор кластеров по id
                    Clustering<Cluster<StatusesCluster>, StatusesCluster> macroClustering = new Clustering<>();
                    for (DbscanPoint point: incomingPoints) { //statefulDbscan.getAllPoints()) {
                        if (point.isAssigned()) {
                            if (macroClustering.findClusterById(point.getClusterId()) == null) {
                                Cluster<StatusesCluster> cluster = new Cluster<>(point.getClusterId(), denStream.lambda);
                                cluster.assignPoint(((SimplifiedDbscanStatusesCluster)point).getStatusesCluster());
                                macroClustering.addCluster(cluster);
                            } else {
                                Cluster<StatusesCluster> cluster = macroClustering.findClusterById(point.getClusterId());
                                cluster.assignPoint(((SimplifiedDbscanStatusesCluster)point).getStatusesCluster());
                            }
                        }
                    }
                    // т.к окно вызывается каждую минуту, то для сохранения статистики каждые 5 минут нужно каждые 5 раз вызывать emit
//                    if (++denStream.executeCounter % 5 == 0) {
                        denStream.executeCounter++;
                        Timestamp time = new Timestamp(new Date().getTime());
                        for (Cluster<StatusesCluster> cluster : macroClustering.getClusters())
                            statisticsDao.saveStatistics(denStream.getClusterStatistics(cluster, time));
//                    }
                }
                try {
                    Thread.sleep(100 - (System.currentTimeMillis() - start));
                }
                catch (InterruptedException ignore) {}
                catch (IllegalArgumentException ex) {
                    System.out.println("Не успеваю!");
                }
            } while (line != null);
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

}
