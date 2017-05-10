package diploma.clustering;

import diploma.clustering.clusters.Cluster;
import diploma.clustering.clusters.Clustering;
import diploma.clustering.clusters.StatusesCluster;
import diploma.clustering.clusters.StatusesClustering;
import diploma.clustering.dbscan.Dbscan;
import diploma.clustering.dbscan.points.DbscanPoint;
import diploma.clustering.dbscan.points.DbscanStatusesCluster;
import diploma.clustering.dbscan.points.SimplifiedDbscanStatusesCluster;
import diploma.statistics.dao.MacroClusteringStatisticsDao;
import diploma.statistics.dao.TweetDao;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Никита
 */
public class MapDbDenStream extends DenStream {
    private Atomic.Var<StatusesClustering> mapDbPotentialMicroClustering;
    private Atomic.Var<StatusesClustering> mapDbOutlierMicroClustering;
    private DB db;

    public MapDbDenStream(int minPoints, int mu, double beta, double lambda, double initSimilarity) {
        super(minPoints, mu, beta, lambda, initSimilarity);
        do {
            try {
                db = DBMaker.fileDB("D:\\MSU\\diploma\\mapdb-clustering\\micro-clusters.mapdb")
                        .closeOnJvmShutdown()
                        .make();
                mapDbPotentialMicroClustering = db
                        .atomicVar("potentialMicroClustering", new StatusesClustering.MapDbSerializer())
                        .createOrOpen();
                mapDbOutlierMicroClustering = db
                        .atomicVar("outlierMicroClustering", new StatusesClustering.MapDbSerializer())
                        .createOrOpen();
                if (mapDbPotentialMicroClustering.get() != null)
//            mapDbPotentialMicroClustering.set(this.potentialMicroClustering);
//        else
                    this.potentialMicroClustering = mapDbPotentialMicroClustering.get();
                if (mapDbOutlierMicroClustering.get() != null)
//            mapDbOutlierMicroClustering.set(this.outlierMicroClustering);
//        else
                    this.outlierMicroClustering = mapDbOutlierMicroClustering.get();
            }
            catch (Exception ex) {
                continue;
            }
            break;
        } while (true);
        db.close();
    }

//    @Override
//    public StatusesClustering getPotentialMicroClustering() {
//        return mapDbPotentialMicroClustering.get();
//    }
//
//    @Override
//    public StatusesClustering getOutlierMicroClustering() {
//        return mapDbOutlierMicroClustering.get();
//    }

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
        MapDbDenStream denStream = new MapDbDenStream(10, 20, 10.0, 0.000001, 0.2);

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
                        if (numberOfDocuments == 150000) {
                            break;
                        }
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
//                if (numberOfDocuments % 5000 == 0) {
                if (false) {
                    List<DbscanStatusesCluster> incomingPoints = new ArrayList<>();
                    for (StatusesCluster cluster : denStream.getPotentialMicroClustering().getClusters()) {
                        // освобождаем чуток памяти
                        if (cluster.getTfIdf().getTermFrequencyMap().size() > 150)
                            cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(150, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
                        incomingPoints.add(new SimplifiedDbscanStatusesCluster(cluster, minNumberOfCommonTerms, cluster.getMacroClusterId()));
                    }

                    for (StatusesCluster cluster : denStream.getOutlierMicroClustering().getClusters()) {
                        if (cluster.getTfIdf().getTermFrequencyMap().size() > 150)
                            cluster.getTfIdf().setTermFrequencyMap(MapUtil.putFirstEntries(150, MapUtil.sortByValue(cluster.getTfIdf().getTermFrequencyMap())));
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
//                    denStream.executeCounter++;
                    Timestamp time = new Timestamp(new Date().getTime());
                    for (Cluster<StatusesCluster> cluster : macroClustering.getClusters())
                        statisticsDao.saveStatistics(denStream.getClusterStatistics(cluster, time,
                                numberOfDocuments - numberOfDocumentsIgnored, 100,
                                denStream.getPotentialMicroClustering().getClusters().size()));
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
}
