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
import diploma.statistics.RemovedMicroClusterStatistics;
import diploma.statistics.dao.MacroClusteringStatisticsDao;
import diploma.statistics.dao.RemovedMicroClusterDao;
import diploma.statistics.dao.TweetDao;
import twitter4j.Status;
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
    /* Показывает нужно ли выполнять DBSCAN для инициализации */
    private boolean initialized = true;
    private List<StatusDbscanPoint> initBuffer = new ArrayList<>();
    private int minPoints;
    /* Показывает нужно ли учитывать подклстера */
    private boolean withSubClustering = false;
    /* Минимальный размер микрокластера, для начала 10 - норм */
    private int mu;
    /* Коэффициент попадания в потенциальный микрокластер, для начала 5.0 - норм */
    private double beta;
    /* Чем больше lambda, тем меньше вес старых кластеров,
     * для начального значения норм около 0,0000001 */
    protected double lambda;
    private double minSimilarity;
    /* Минимальное время превращения потенциального микрокластера в шум. */
    private double tp;
    private int numberOfProcessedUnits;
    private int statisticsCounter = 0;
    protected StatusesClustering outlierMicroClustering;
    protected StatusesClustering potentialMicroClustering;
    protected RemovedMicroClusterDao removedMicroClusterDao;
    private int executeCounter = 0;
    private long currentTimestamp = 0;

//    public DenStream() {}

    public DenStream(int minPoints, int mu, double beta, double lambda, double minSimilarity) {
        this.minPoints = minPoints;
        this.mu = mu;
        this.beta = beta;
        this.lambda = lambda;
        this.minSimilarity = minSimilarity;
        this.outlierMicroClustering = new StatusesClustering(minSimilarity);
        this.potentialMicroClustering = new StatusesClustering(minSimilarity);
        this.tp = Math.round(1 / lambda * Math.log((beta * mu) / (beta * mu - 1))) + 1;
        this.removedMicroClusterDao = new RemovedMicroClusterDao();
    }

    public StatusesClustering getPotentialMicroClustering() {
        return potentialMicroClustering;
    }

    public StatusesClustering getOutlierMicroClustering() {
        return outlierMicroClustering;
    }

    public void processNext(EnhancedStatus status) {
        numberOfProcessedUnits++;
        currentTimestamp++;
        status.setCreationTimestamp(currentTimestamp);
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
                    if (nearestCluster.getWeight(currentTimestamp) > beta * mu) {
                        getOutlierMicroClustering().getClusters().remove(nearestCluster);
                        /* Меняем id кластера, тк среди потенциальных микрокластеров
                            уже может быть кластер с таким id */
                        nearestCluster.setId(getPotentialMicroClustering().getLastClusterId() + 1);
                        getPotentialMicroClustering().addCluster(nearestCluster, currentTimestamp);
                    }
                }
            }
            if (!merged) {
                StatusesCluster newCluster = new StatusesCluster(getOutlierMicroClustering().getLastClusterId() + 1, lambda);
                newCluster.assignPoint(status);
                getOutlierMicroClustering().addCluster(newCluster, currentTimestamp);
                if (withSubClustering) newCluster.createSubClustering(minSimilarity, mu, beta);
            }
        }
//        if (numberOfProcessedUnits % 10000 == 0) {
        if (currentTimestamp % tp == 0) {
            ArrayList<StatusesCluster> removalList = new ArrayList<>();
            for (StatusesCluster c : getPotentialMicroClustering().getClusters())
                if (c.getWeight(currentTimestamp) < beta * mu)
                    removalList.add(c);
                // если последние 10 dbscan-ов добавляли в среднем меньше 10 сообщений, то удаляем кластер
                else if (c.getRatePerUnitQueue().size() == 10 && c.getMeanRatePerUnit() < 10)
                    removalList.add(c);
                else if (withSubClustering) {
                    List<StatusesCluster> subClustersRemovalList = new ArrayList<>();
                    for (StatusesCluster subCluster : c.getPotentialSubClustering().getClusters())
                        if (subCluster.getWeight(currentTimestamp) < beta * mu) subClustersRemovalList.add(subCluster);
                    for (StatusesCluster subCluster : subClustersRemovalList)
                        c.getPotentialSubClustering().getClusters().remove(subCluster);

                    subClustersRemovalList.clear();
                    for (StatusesCluster subCluster : c.getOutlierSubClustering().getClusters()) {
                        long t0 = subCluster.getCreationTime();
                        double xsi1 = Math.pow(2, (-lambda * (System.currentTimeMillis() - t0 + tp))) - 1;
                        double xsi2 = Math.pow(2, -lambda * tp) - 1;
                        double xsi = xsi1 / xsi2;
                        if (subCluster.getWeight(currentTimestamp) < xsi)
                            removalList.add(c);
                    }
                    for (StatusesCluster subCluster : subClustersRemovalList)
                        c.getOutlierSubClustering().getClusters().remove(subCluster);
                }
            for (StatusesCluster c : removalList) {
                removedMicroClusterDao.saveStatistics(getRemovedMicroClusterStatistics(c, (byte) 1));
                getPotentialMicroClustering().getClusters().remove(c);
            }

            removalList.clear();
            for (StatusesCluster c : getOutlierMicroClustering().getClusters()) {
                long t0 = c.getCreationTime();
                double xsi1 = Math.pow(2, (-lambda * (currentTimestamp - t0 + tp))) - 1;
                double xsi2 = Math.pow(2, -lambda * tp) - 1;
                double xsi = xsi1 / xsi2;
                if (c.getWeight(currentTimestamp) < xsi)
                    removalList.add(c);
            }
            for (StatusesCluster c : removalList) {
//                removedMicroClusterDao.saveStatistics(getRemovedMicroClusterStatistics(c, (byte) 0));
                getOutlierMicroClustering().getClusters().remove(c);
            }
        }
    }

    public int getNumberOfProcessedUnits() {
        return numberOfProcessedUnits;
    }

    private void initialDbscan() {
        Dbscan dbscan = new Dbscan(minPoints, minSimilarity);
        dbscan.run(initBuffer);
        // создаем начальные кластера из того, что получилось в DBSCAN
        for (StatusDbscanPoint point: initBuffer) {
            if (!point.isNoise()) {
                if (getPotentialMicroClustering().findClusterById(point.getClusterId()) == null) {
                    StatusesCluster cluster = new StatusesCluster(point.getClusterId(), lambda);
                    cluster.assignPoint(point.getStatus());
                    getPotentialMicroClustering().addCluster(cluster, currentTimestamp);
                }
                else {
                    StatusesCluster cluster = getPotentialMicroClustering().findClusterById(point.getClusterId());
                    cluster.assignPoint(point.getStatus());
                }
            }
        }
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
        TweetDao tweetDao = new TweetDao();
        int numberOfDocuments = 0;
        int numberOfDocumentsIgnored = 0;
        int minNumberOfCommonTerms = 6;
//        DenStream denStream = new DenStream(10, 20, 10.0, -Math.log(3.0) / Math.log(2)/(double) 400, 0.4);
        DenStream denStream = new DenStream(10, 20, 10.0, 0.000001, 0.4);

        try (BufferedReader br = new BufferedReader(new FileReader("D:\\MSU\\diploma\\tweets-sets\\2017-04-09-sport-events.txt"))) {
            String line = null;
            int i = 0;
            do {
                long start = System.currentTimeMillis();
                line = br.readLine();
                if (line != null && !line.equals("")) {
                    EnhancedStatus status = null;
                    try {
                        numberOfDocuments++;
                        Status tweet = TwitterObjectFactory.createStatus(line);
//                        tweetDao.saveTweet(TwitterObjectFactory.createStatus(line));

                        String userScreenName = tweet.getUser().getScreenName().toLowerCase();
                        String retweetedUserScreenName = "";
                        if (tweet.getRetweetedStatus() != null)
                            retweetedUserScreenName = tweet.getRetweetedStatus().getUser().getScreenName().toLowerCase();
                        List<String> ignoredUsers = Arrays.asList("petebetnow", "paddyspower1", "bingobestodds", "highrisklife1", "paddyspower2",
                                "mufcfergie", "kim_feeney1", "roadtoprofituk", "olbg", "earnathomeuk");
                        if (!(ignoredUsers.contains(userScreenName) || ignoredUsers.contains(retweetedUserScreenName))) {
                            status = new EnhancedStatus(tweet);
                            if (!"".equals(status.getNormalizedText()) && status.getNormalizedText().split(" ").length >= 4)
                                denStream.processNext(status);
                        }
                        else numberOfDocumentsIgnored++;
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

                // TODO: Доделать также, как в шторме
                if (numberOfDocuments % 10000 == 0) {
//                if (false) {
                    List<DbscanStatusesCluster> incomingPoints = new ArrayList<>();
                    for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters()) {
                        // освобождаем чуток памяти
                        if (cluster.getTfIdf().getTermFrequencyMap().size() > 1000)
                            cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(1000, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
                        incomingPoints.add(new SimplifiedDbscanStatusesCluster(cluster, minNumberOfCommonTerms, cluster.getMacroClusterId()));
                    }

                    for (StatusesCluster cluster : denStream.getOutlierMicroClustering().getClusters()) {
                        if (cluster.getTfIdf().getTermFrequencyMap().size() > 1000)
                            cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(1000, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
                    }

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
                            StatusesCluster statusesCluster = ((SimplifiedDbscanStatusesCluster) point).getStatusesCluster();
                            if (macroClustering.findClusterById(point.getClusterId()) == null) {
//                                if (statusesCluster.getMacroClusterId() == 0)
                                statusesCluster.setMacroClusterId(point.getClusterId());
                                Cluster<StatusesCluster> cluster = new Cluster<>(point.getClusterId(), denStream.lambda);
                                cluster.assignPoint(statusesCluster);
                                macroClustering.addCluster(cluster);
                            } else {
                                Cluster<StatusesCluster> cluster = macroClustering.findClusterById(point.getClusterId());
                                statusesCluster.setMacroClusterId(point.getClusterId());
                                cluster.assignPoint(statusesCluster);
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

                    for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters())
                        cluster.resetProcessedPerTimeUnit();

                    for (StatusesCluster cluster : denStream.getOutlierMicroClustering().getClusters())
                        cluster.resetProcessedPerTimeUnit();
                }
//                try {
//                    Thread.sleep(100 - (System.currentTimeMillis() - start));
//                }
//                catch (InterruptedException ignore) {}
//                catch (IllegalArgumentException ex) {
//                    System.out.println("Не успеваю!");
//                }
            } while (line != null);
//            denStream.mapDbOutlierMicroClustering.set(denStream.outlierMicroClustering);
//            denStream.mapDbPotentialMicroClustering.set(denStream.potentialMicroClustering);
//            denStream.db.close();
            br.close();
        }
        catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    protected MacroClusteringStatistics getClusterStatistics(Cluster<StatusesCluster> cluster, Timestamp time) {
        MacroClusteringStatistics statistics = new MacroClusteringStatistics();
        int totalNumberOfDocuments = 0;
        int totalProcessedPerTimeUnit = 0;
        Map<String, Integer> topTenTerms = new HashMap<>();
        for (StatusesCluster statusesCluster: cluster.getAssignedPoints()) {
            totalNumberOfDocuments += statusesCluster.getTfIdf().getDocumentNumber();
            totalProcessedPerTimeUnit += statusesCluster.getProcessedPerTimeUnit();
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
        statistics.setTotalProcessedPerTimeUnit(totalProcessedPerTimeUnit);
        return statistics;
    }

    protected RemovedMicroClusterStatistics getRemovedMicroClusterStatistics(StatusesCluster cluster, byte isPotential) {
        RemovedMicroClusterStatistics statistics = new RemovedMicroClusterStatistics();
        statistics.setCreationTime(cluster.getActualCreationTime());
        statistics.setLastUpdateTIme(cluster.getActualUpdateTime());
        statistics.setNumberOfDocuments(cluster.getSize());
        statistics.setIsPotential(isPotential);
        Map<String, Integer> sortedTopTen = MapUtil.putFirstEntries(10, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap()));
        statistics.setTopWords(sortedTopTen.toString());
        return statistics;
    }

}
